package com.sayanjalinexus.meshchat.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.sayanjalinexus.meshchat.core.model.Peer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production [BleTransport] backed by [android.bluetooth.le.BluetoothLeScanner].
 *
 * Permission checks are the caller's responsibility (see
 * [com.sayanjalinexus.meshchat.data.PeerRepositoryImpl], which consults
 * [PermissionsManager] before ever calling [scanResults]) — the
 * `@SuppressLint("MissingPermission")` annotations here document that this
 * class trusts its caller rather than re-checking permissions itself.
 */
@Singleton
class AndroidBleTransport @Inject constructor(
    @ApplicationContext private val context: Context,
) : BleTransport {

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    override fun isBluetoothSupported(): Boolean = bluetoothManager?.adapter != null

    override fun isBluetoothEnabled(): Boolean = bluetoothManager?.adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    override fun scanResults(): Flow<BleScanEvent> = callbackFlow {
        val scanner = bluetoothManager?.adapter?.bluetoothLeScanner
        if (scanner == null) {
            trySend(BleScanEvent.Failure(BleScanFailureReason.ADAPTER_UNAVAILABLE))
            close()
            return@callbackFlow
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(BleScanEvent.Result(result.toPeer()))
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { trySend(BleScanEvent.Result(it.toPeer())) }
            }

            override fun onScanFailed(errorCode: Int) {
                trySend(BleScanEvent.Failure(BleScanFailureReason.fromNativeCode(errorCode)))
            }
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(BleConstants.MESH_SERVICE_UUID))
                .build(),
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setReportDelay(BleConstants.SCAN_REPORT_DELAY_MS)
            .build()

        scanner.startScan(filters, settings, callback)

        awaitClose { scanner.stopScan(callback) }
    }

    @SuppressLint("MissingPermission")
    private fun ScanResult.toPeer(): Peer {
        // Reading device.name requires BLUETOOTH_CONNECT on API 31+; if the
        // permission is somehow missing this throws SecurityException rather
        // than returning null, so it's wrapped defensively.
        val name = runCatching { device?.name }.getOrNull()
        return Peer(
            address = device.address,
            nickname = name,
            rssi = rssi,
            lastSeenAt = System.currentTimeMillis(),
        )
    }
}
