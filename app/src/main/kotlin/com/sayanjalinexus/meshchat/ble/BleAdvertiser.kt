package com.sayanjalinexus.meshchat.ble

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the platform BLE advertising APIs — the counterpart to
 * [BleTransport] for the "be discoverable" half of peer discovery.
 *
 * [AndroidBleAdvertiser] is the production implementation. Tests
 * substitute a fake so [com.sayanjalinexus.meshchat.data.AdvertisingRepositoryImpl]
 * can be verified without touching real Bluetooth hardware.
 */
interface BleAdvertiser {

    /** Whether this device has a Bluetooth radio at all. */
    fun isBluetoothSupported(): Boolean

    /** Whether Bluetooth is currently turned on. */
    fun isBluetoothEnabled(): Boolean

    /**
     * Whether this device's Bluetooth chipset supports BLE peripheral-mode
     * advertising. Not all BLE-capable devices do — this is a distinct
     * capability from being able to *scan*.
     */
    fun isAdvertisingSupported(): Boolean

    /**
     * Cold flow: starts advertising the mesh service UUID when collected,
     * stops when the collecting coroutine is cancelled. Emits exactly one
     * [AdvertiseEvent] (start success or failure) per collection.
     */
    fun advertise(): Flow<AdvertiseEvent>
}
