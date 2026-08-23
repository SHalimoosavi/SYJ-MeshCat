package com.sayanjalinexus.meshchat.data

import com.sayanjalinexus.meshchat.ble.AdvertiseEvent
import com.sayanjalinexus.meshchat.ble.AdvertiseFailureReason
import com.sayanjalinexus.meshchat.ble.AdvertiseState
import com.sayanjalinexus.meshchat.ble.BleAdvertiser
import com.sayanjalinexus.meshchat.ble.PermissionsManager
import com.sayanjalinexus.meshchat.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [AdvertisingRepository] implementation.
 *
 * Mirrors [PeerRepositoryImpl]'s lifecycle pattern: [startAdvertising]
 * verifies Bluetooth availability, chipset advertising support, and
 * permissions before subscribing to [BleAdvertiser.advertise];
 * [stopAdvertising] cancels that subscription, which — since
 * [BleAdvertiser.advertise] is a cold `callbackFlow` — stops the
 * underlying radio advertisement too.
 */
@Singleton
class AdvertisingRepositoryImpl @Inject constructor(
    private val bleAdvertiser: BleAdvertiser,
    private val permissionsManager: PermissionsManager,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : AdvertisingRepository {

    private val _advertiseState = MutableStateFlow<AdvertiseState>(AdvertiseState.Idle)
    override val advertiseState: StateFlow<AdvertiseState> = _advertiseState.asStateFlow()

    private var advertiseJob: Job? = null

    override fun startAdvertising() {
        if (advertiseJob?.isActive == true) return

        if (!bleAdvertiser.isBluetoothSupported()) {
            _advertiseState.value = AdvertiseState.Unsupported
            return
        }
        if (!bleAdvertiser.isBluetoothEnabled()) {
            _advertiseState.value = AdvertiseState.BluetoothDisabled
            return
        }
        if (!bleAdvertiser.isAdvertisingSupported()) {
            _advertiseState.value = AdvertiseState.Unsupported
            return
        }
        if (!permissionsManager.hasAllPermissions()) {
            _advertiseState.value = AdvertiseState.PermissionsRequired
            return
        }

        advertiseJob = bleAdvertiser.advertise()
            .onEach(::handleAdvertiseEvent)
            .catch { _advertiseState.value = AdvertiseState.Error(AdvertiseFailureReason.UNKNOWN) }
            .launchIn(applicationScope)
    }

    override fun stopAdvertising() {
        advertiseJob?.cancel()
        advertiseJob = null
        if (_advertiseState.value == AdvertiseState.Advertising) {
            _advertiseState.value = AdvertiseState.Idle
        }
    }

    private fun handleAdvertiseEvent(event: AdvertiseEvent) {
        when (event) {
            is AdvertiseEvent.Started -> _advertiseState.value = AdvertiseState.Advertising
            is AdvertiseEvent.Failure -> _advertiseState.value = AdvertiseState.Error(event.reason)
        }
    }
}
