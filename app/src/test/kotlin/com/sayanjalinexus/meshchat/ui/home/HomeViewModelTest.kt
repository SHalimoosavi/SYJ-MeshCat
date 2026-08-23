package com.sayanjalinexus.meshchat.ui.home

import com.sayanjalinexus.meshchat.ble.AdvertiseState
import com.sayanjalinexus.meshchat.ble.BleScanState
import com.sayanjalinexus.meshchat.ble.PermissionsManager
import com.sayanjalinexus.meshchat.core.TestDispatcherProvider
import com.sayanjalinexus.meshchat.core.model.Peer
import com.sayanjalinexus.meshchat.data.AdvertisingRepository
import com.sayanjalinexus.meshchat.data.PeerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [HomeViewModel] has no recurring/self-rescheduling coroutines of its own
 * (unlike [com.sayanjalinexus.meshchat.data.PeerRepositoryImpl]'s eviction
 * loop), so `advanceUntilIdle()` is safe here: every launched coroutine
 * genuinely runs to a resting suspension point rather than perpetually
 * rescheduling itself.
 */
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakePeers = MutableStateFlow<List<Peer>>(emptyList())
    private val fakeScanState = MutableStateFlow<BleScanState>(BleScanState.Idle)
    private val fakeAdvertiseState = MutableStateFlow<AdvertiseState>(AdvertiseState.Idle)

    private lateinit var peerRepository: PeerRepository
    private lateinit var advertisingRepository: AdvertisingRepository
    private lateinit var permissionsManager: PermissionsManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        peerRepository = mockk(relaxed = true) {
            every { peers } returns fakePeers
            every { scanState } returns fakeScanState
        }
        advertisingRepository = mockk(relaxed = true) {
            every { advertiseState } returns fakeAdvertiseState
        }
        permissionsManager = mockk {
            every { hasAllPermissions() } returns true
            every { requiredPermissions() } returns arrayOf("android.permission.BLUETOOTH_SCAN")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = HomeViewModel(
        dispatcherProvider = TestDispatcherProvider(testDispatcher),
        peerRepository = peerRepository,
        advertisingRepository = advertisingRepository,
        permissionsManager = permissionsManager,
    )

    @Test
    fun `home view model loads scaffold status message on init`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Milestone 4: BLE advertising online.", state.statusMessage)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `home view model reflects PeerRepository scan state and peers`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeScanState.value = BleScanState.Scanning
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(BleScanState.Scanning, viewModel.uiState.value.bleScanState)

        val peer = Peer(address = "AA:BB:CC:DD:EE:FF", nickname = "node-1", rssi = -50, lastSeenAt = 0L)
        fakePeers.value = listOf(peer)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(peer), viewModel.uiState.value.peers)
    }

    @Test
    fun `home view model reflects AdvertisingRepository state`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeAdvertiseState.value = AdvertiseState.Advertising
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AdvertiseState.Advertising, viewModel.uiState.value.advertiseState)
    }

    @Test
    fun `onPermissionsResult with denial sets PermissionsRequired state`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPermissionsResult(allGranted = false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(BleScanState.PermissionsRequired, viewModel.uiState.value.bleScanState)
    }

    @Test
    fun `onPermissionsResult with grant starts both scanning and advertising`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPermissionsResult(allGranted = true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { peerRepository.startScanning() }
        verify { advertisingRepository.startAdvertising() }
    }

    @Test
    fun `onToggleScanRequested while scanning stops both scanning and advertising`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        fakeScanState.value = BleScanState.Scanning
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onToggleScanRequested()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { peerRepository.stopScanning() }
        verify { advertisingRepository.stopAdvertising() }
    }
}
