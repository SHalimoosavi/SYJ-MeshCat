package com.sayanjalinexus.meshchat.ble

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Test double for [BleTransport]. `replay = 1` on [events] means an event
 * emitted before the repository's collector subscribes is still delivered
 * once it does — removing timing-order fragility from tests that emit
 * immediately after starting scanning.
 */
class FakeBleTransport : BleTransport {

    var bluetoothSupported: Boolean = true
    var bluetoothEnabled: Boolean = true

    private val events = MutableSharedFlow<BleScanEvent>(replay = 1, extraBufferCapacity = 16)

    var startScanCallCount: Int = 0
        private set

    override fun isBluetoothSupported(): Boolean = bluetoothSupported

    override fun isBluetoothEnabled(): Boolean = bluetoothEnabled

    override fun scanResults(): Flow<BleScanEvent> {
        startScanCallCount++
        return events.asSharedFlow()
    }

    fun emit(event: BleScanEvent) {
        events.tryEmit(event)
    }
}
