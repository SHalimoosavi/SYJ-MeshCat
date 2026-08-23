package com.sayanjalinexus.meshchat.ble

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Test double for [BleAdvertiser]. `replay = 1` on [events] means an event
 * emitted before the repository's collector subscribes is still delivered
 * once it does — removing timing-order fragility from tests that emit
 * immediately after starting advertising (same rationale as
 * [FakeBleTransport]).
 */
class FakeBleAdvertiser : BleAdvertiser {

    var bluetoothSupported: Boolean = true
    var bluetoothEnabled: Boolean = true
    var advertisingSupported: Boolean = true

    private val events = MutableSharedFlow<AdvertiseEvent>(replay = 1, extraBufferCapacity = 16)

    var advertiseCallCount: Int = 0
        private set

    override fun isBluetoothSupported(): Boolean = bluetoothSupported

    override fun isBluetoothEnabled(): Boolean = bluetoothEnabled

    override fun isAdvertisingSupported(): Boolean = advertisingSupported

    override fun advertise(): Flow<AdvertiseEvent> {
        advertiseCallCount++
        return events.asSharedFlow()
    }

    fun emit(event: AdvertiseEvent) {
        events.tryEmit(event)
    }
}
