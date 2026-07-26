package com.sayanjalinexus.meshchat.ble

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the platform BLE scanning APIs.
 *
 * [AndroidBleTransport] is the production implementation. Tests substitute
 * a fake so [com.sayanjalinexus.meshchat.data.PeerRepositoryImpl] can be
 * verified without touching real Bluetooth hardware — see
 * ARCHITECTURE.md's data-source layer for how this fits into the app.
 */
interface BleTransport {

    /** Whether this device has a Bluetooth radio at all. */
    fun isBluetoothSupported(): Boolean

    /** Whether Bluetooth is currently turned on. */
    fun isBluetoothEnabled(): Boolean

    /**
     * Cold flow: starts scanning for mesh peers when collected, stops
     * scanning when the collecting coroutine is cancelled. Emits one
     * [BleScanEvent] per discovered/updated peer or scan failure.
     */
    fun scanResults(): Flow<BleScanEvent>
}
