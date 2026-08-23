package com.sayanjalinexus.meshchat.data

import com.sayanjalinexus.meshchat.ble.BleConstants
import com.sayanjalinexus.meshchat.ble.BleScanEvent
import com.sayanjalinexus.meshchat.ble.BleScanFailureReason
import com.sayanjalinexus.meshchat.ble.BleScanState
import com.sayanjalinexus.meshchat.ble.BleTransport
import com.sayanjalinexus.meshchat.ble.PermissionsManager
import com.sayanjalinexus.meshchat.core.DispatcherProvider
import com.sayanjalinexus.meshchat.core.model.Peer
import com.sayanjalinexus.meshchat.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [PeerRepository] implementation.
 *
 * Owns the lifecycle of [BleTransport] scanning: [startScanning] verifies
 * Bluetooth availability and permissions before subscribing to
 * [BleTransport.scanResults]; [stopScanning] cancels that subscription —
 * since [BleTransport.scanResults] is a cold `callbackFlow`, cancelling the
 * collector stops the underlying radio scan too.
 *
 * A background eviction loop prunes peers not seen within
 * [BleConstants.PEER_STALE_TIMEOUT_MS], since BLE visibility is inherently
 * transient: a peer walking out of range never sends an explicit
 * "goodbye" packet.
 */
@Singleton
class PeerRepositoryImpl @Inject constructor(
    private val bleTransport: BleTransport,
    private val permissionsManager: PermissionsManager,
    private val dispatcherProvider: DispatcherProvider,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : PeerRepository {

    private val peersByAddress = MutableStateFlow<Map<String, Peer>>(emptyMap())
    private val _scanState = MutableStateFlow<BleScanState>(BleScanState.Idle)

    override val peers: StateFlow<List<Peer>> = peersByAddress
        .map { byAddress -> byAddress.values.sortedByDescending(Peer::lastSeenAt) }
        .stateIn(applicationScope, SharingStarted.Eagerly, emptyList())

    override val scanState: StateFlow<BleScanState> = _scanState.asStateFlow()

    private var scanJob: Job? = null

    init {
        startEvictionLoop()
    }

    @Suppress("ReturnCount") // early-return guard clauses for Bluetooth support/enabled/permissions checks
    override fun startScanning() {
        if (scanJob?.isActive == true) return

        if (!bleTransport.isBluetoothSupported()) {
            _scanState.value = BleScanState.Unsupported
            return
        }
        if (!bleTransport.isBluetoothEnabled()) {
            _scanState.value = BleScanState.BluetoothDisabled
            return
        }
        if (!permissionsManager.hasAllPermissions()) {
            _scanState.value = BleScanState.PermissionsRequired
            return
        }

        _scanState.value = BleScanState.Scanning
        scanJob = bleTransport.scanResults()
            .onEach(::handleScanEvent)
            .catch { _scanState.value = BleScanState.Error(BleScanFailureReason.UNKNOWN) }
            .launchIn(applicationScope)
    }

    override fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
        if (_scanState.value == BleScanState.Scanning) {
            _scanState.value = BleScanState.Idle
        }
    }

    private fun handleScanEvent(event: BleScanEvent) {
        when (event) {
            is BleScanEvent.Result -> {
                peersByAddress.value = peersByAddress.value + (event.peer.address to event.peer)
            }
            is BleScanEvent.Failure -> {
                _scanState.value = BleScanState.Error(event.reason)
            }
        }
    }

    private fun startEvictionLoop() {
        applicationScope.launch(dispatcherProvider.default) {
            while (true) {
                delay(BleConstants.PEER_EVICTION_INTERVAL_MS)
                val cutoff = System.currentTimeMillis() - BleConstants.PEER_STALE_TIMEOUT_MS
                peersByAddress.value = peersByAddress.value.filterValues { it.lastSeenAt >= cutoff }
            }
        }
    }
}
