package com.sayanjalinexus.meshchat.data

import com.sayanjalinexus.meshchat.ble.AdvertiseEvent
import com.sayanjalinexus.meshchat.ble.AdvertiseState
import com.sayanjalinexus.meshchat.ble.FakeBleAdvertiser
import com.sayanjalinexus.meshchat.ble.PermissionsManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unlike [PeerRepositoryImpl], [AdvertisingRepositoryImpl] has no recurring
 * background loop, so `testDispatcher.scheduler.runCurrent()` is enough to
 * drain its single-shot advertise-event collector — no risk of the
 * infinite-loop hazard that rules out `advanceUntilIdle()` in
 * `PeerRepositoryImplTest`.
 */
class AdvertisingRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var bleAdvertiser: FakeBleAdvertiser
    private lateinit var permissionsManager: PermissionsManager

    @Before
    fun setUp() {
        bleAdvertiser = FakeBleAdvertiser()
        permissionsManager = mockk {
            every { hasAllPermissions() } returns true
        }
    }

    private fun buildRepository(scope: kotlinx.coroutines.CoroutineScope) = AdvertisingRepositoryImpl(
        bleAdvertiser = bleAdvertiser,
        permissionsManager = permissionsManager,
        applicationScope = scope,
    )

    @Test
    fun `startAdvertising without permissions sets PermissionsRequired and does not advertise`() = runTest(testDispatcher) {
        every { permissionsManager.hasAllPermissions() } returns false
        val repository = buildRepository(this)

        repository.startAdvertising()

        assertEquals(AdvertiseState.PermissionsRequired, repository.advertiseState.value)
        assertEquals(0, bleAdvertiser.advertiseCallCount)
    }

    @Test
    fun `startAdvertising when bluetooth unsupported sets Unsupported state`() = runTest(testDispatcher) {
        bleAdvertiser.bluetoothSupported = false
        val repository = buildRepository(this)

        repository.startAdvertising()

        assertEquals(AdvertiseState.Unsupported, repository.advertiseState.value)
    }

    @Test
    fun `startAdvertising when bluetooth disabled sets BluetoothDisabled state`() = runTest(testDispatcher) {
        bleAdvertiser.bluetoothEnabled = false
        val repository = buildRepository(this)

        repository.startAdvertising()

        assertEquals(AdvertiseState.BluetoothDisabled, repository.advertiseState.value)
    }

    @Test
    fun `startAdvertising when chipset cannot advertise sets Unsupported state`() = runTest(testDispatcher) {
        bleAdvertiser.advertisingSupported = false
        val repository = buildRepository(this)

        repository.startAdvertising()

        assertEquals(AdvertiseState.Unsupported, repository.advertiseState.value)
    }

    @Test
    fun `successful start event transitions state to Advertising`() = runTest(testDispatcher) {
        val repository = buildRepository(this)

        repository.startAdvertising()
        bleAdvertiser.emit(AdvertiseEvent.Started)
        testDispatcher.scheduler.runCurrent()

        assertEquals(AdvertiseState.Advertising, repository.advertiseState.value)
        assertEquals(1, bleAdvertiser.advertiseCallCount)
    }

    @Test
    fun `failure event transitions state to Error with the reported reason`() = runTest(testDispatcher) {
        val repository = buildRepository(this)

        repository.startAdvertising()
        bleAdvertiser.emit(AdvertiseEvent.Failure(com.sayanjalinexus.meshchat.ble.AdvertiseFailureReason.TOO_MANY_ADVERTISERS))
        testDispatcher.scheduler.runCurrent()

        assertEquals(
            AdvertiseState.Error(com.sayanjalinexus.meshchat.ble.AdvertiseFailureReason.TOO_MANY_ADVERTISERS),
            repository.advertiseState.value,
        )
    }

    @Test
    fun `stopAdvertising cancels the collector and resets state to Idle`() = runTest(testDispatcher) {
        val repository = buildRepository(this)
        repository.startAdvertising()
        bleAdvertiser.emit(AdvertiseEvent.Started)
        testDispatcher.scheduler.runCurrent()
        assertEquals(AdvertiseState.Advertising, repository.advertiseState.value)

        repository.stopAdvertising()

        assertEquals(AdvertiseState.Idle, repository.advertiseState.value)
    }

    @Test
    fun `calling startAdvertising twice does not double-subscribe`() = runTest(testDispatcher) {
        val repository = buildRepository(this)

        repository.startAdvertising()
        repository.startAdvertising()

        assertEquals(1, bleAdvertiser.advertiseCallCount)
    }
}
