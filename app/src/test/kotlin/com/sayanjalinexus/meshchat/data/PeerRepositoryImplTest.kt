package com.sayanjalinexus.meshchat.data

import com.sayanjalinexus.meshchat.ble.BleScanEvent
import com.sayanjalinexus.meshchat.ble.BleScanState
import com.sayanjalinexus.meshchat.ble.FakeBleTransport
import com.sayanjalinexus.meshchat.ble.PermissionsManager
import com.sayanjalinexus.meshchat.core.TestDispatcherProvider
import com.sayanjalinexus.meshchat.core.model.Peer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [PeerRepositoryImpl] runs a *recurring* eviction loop (`while (true) { delay(...); ... }`),
 * so these tests deliberately use `testDispatcher.scheduler.runCurrent()` —
 * which only executes tasks already due — rather than `advanceUntilIdle()`,
 * which would spin forever chasing a queue that never empties.
 */
class PeerRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var bleTransport: FakeBleTransport
    private lateinit var permissionsManager: PermissionsManager

    @Before
    fun setUp() {
        bleTransport = FakeBleTransport()
        permissionsManager = mockk {
            every { hasAllPermissions() } returns true
        }
    }

    private fun buildRepository(scope: kotlinx.coroutines.CoroutineScope) = PeerRepositoryImpl(
        bleTransport = bleTransport,
        permissionsManager = permissionsManager,
        dispatcherProvider = TestDispatcherProvider(testDispatcher),
        applicationScope = scope,
    )

    @Test
    fun `startScanning without permissions sets PermissionsRequired and does not scan`() = runTest(testDispatcher) {
        every { permissionsManager.hasAllPermissions() } returns false
        val repository = buildRepository(backgroundScope)

        repository.startScanning()

        assertEquals(BleScanState.PermissionsRequired, repository.scanState.value)
        assertEquals(0, bleTransport.startScanCallCount)
    }

    @Test
    fun `startScanning when bluetooth unsupported sets Unsupported state`() = runTest(testDispatcher) {
        bleTransport.bluetoothSupported = false
        val repository = buildRepository(backgroundScope)

        repository.startScanning()

        assertEquals(BleScanState.Unsupported, repository.scanState.value)
    }

    @Test
    fun `startScanning when bluetooth disabled sets BluetoothDisabled state`() = runTest(testDispatcher) {
        bleTransport.bluetoothEnabled = false
        val repository = buildRepository(backgroundScope)

        repository.startScanning()

        assertEquals(BleScanState.BluetoothDisabled, repository.scanState.value)
    }

    @Test
    fun `startScanning with everything available sets Scanning state and subscribes to transport`() = runTest(testDispatcher) {
        val repository = buildRepository(backgroundScope)

        repository.startScanning()

        assertEquals(BleScanState.Scanning, repository.scanState.value)
        assertEquals(1, bleTransport.startScanCallCount)
    }

    @Test
    fun `discovered peer appears in peers list`() = runTest(testDispatcher) {
        val repository = buildRepository(backgroundScope)
        repository.startScanning()

        val peer = Peer(
            address = "AA:BB:CC:DD:EE:FF",
            nickname = "node-1",
            rssi = -55,
            lastSeenAt = System.currentTimeMillis(),
        )
        bleTransport.emit(BleScanEvent.Result(peer))
        testDispatcher.scheduler.runCurrent()

        assertTrue(repository.peers.value.contains(peer))
    }

    @Test
    fun `stopScanning cancels the collector and resets state to Idle`() = runTest(testDispatcher) {
        val repository = buildRepository(backgroundScope)
        repository.startScanning()
        assertEquals(BleScanState.Scanning, repository.scanState.value)

        repository.stopScanning()

        assertEquals(BleScanState.Idle, repository.scanState.value)
    }

    @Test
    fun `calling startScanning twice does not double-subscribe`() = runTest(testDispatcher) {
        val repository = buildRepository(backgroundScope)

        repository.startScanning()
        repository.startScanning()

        assertEquals(1, bleTransport.startScanCallCount)
    }
}
