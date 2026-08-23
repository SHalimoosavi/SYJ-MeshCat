package com.sayanjalinexus.meshchat.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production [BleAdvertiser] backed by [android.bluetooth.le.BluetoothLeAdvertiser].
 *
 * Broadcasts only the mesh service UUID ([BleConstants.MESH_SERVICE_UUID])
 * — no device name and no TX power level are included in the advertisement,
 * both to stay well under the 31-byte legacy advertisement size limit and
 * to avoid leaking the device's Bluetooth name. A real, cryptographically
 * verifiable node identity is layered on top of this raw discoverability in
 * Milestone 6 (encryption) — for now, "advertising" just means "a mesh
 * node is nearby", which is exactly what [AndroidBleTransport]'s scan
 * filter is listening for.
 *
 * Permission checks are the caller's responsibility (see
 * [com.sayanjalinexus.meshchat.data.AdvertisingRepositoryImpl], which
 * consults [PermissionsManager] before ever calling [advertise]).
 */
@Singleton
class AndroidBleAdvertiser @Inject constructor(
    @ApplicationContext private val context: Context,
) : BleAdvertiser {

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    override fun isBluetoothSupported(): Boolean = bluetoothManager?.adapter != null

    override fun isBluetoothEnabled(): Boolean = bluetoothManager?.adapter?.isEnabled == true

    override fun isAdvertisingSupported(): Boolean =
        bluetoothManager?.adapter?.isMultipleAdvertisementSupported == true

    @SuppressLint("MissingPermission")
    override fun advertise(): Flow<AdvertiseEvent> = callbackFlow {
        val advertiser = bluetoothManager?.adapter?.bluetoothLeAdvertiser
        if (advertiser == null) {
            trySend(AdvertiseEvent.Failure(AdvertiseFailureReason.ADAPTER_UNAVAILABLE))
            close()
            return@callbackFlow
        }

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                trySend(AdvertiseEvent.Started)
            }

            override fun onStartFailure(errorCode: Int) {
                trySend(AdvertiseEvent.Failure(AdvertiseFailureReason.fromNativeCode(errorCode)))
            }
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .setTimeout(0) // advertise until explicitly stopped
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(BleConstants.MESH_SERVICE_UUID))
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

        advertiser.startAdvertising(settings, data, callback)

        awaitClose { advertiser.stopAdvertising(callback) }
    }
}
