package com.sayanjalinexus.meshchat.ui.home

import app.cash.turbine.test
import com.sayanjalinexus.meshchat.core.TestDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `home view model loads scaffold status message on init`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(
            dispatcherProvider = TestDispatcherProvider(testDispatcher),
        )

        viewModel.uiState.test {
            // 1. Default state, before init{}'s coroutine has run.
            val initial = awaitItem()
            assertEquals(HomeUiState(), initial)

            // 2. isLoading flips true synchronously at the start of loadStatus().
            val loading = awaitItem()
            assertEquals(true, loading.isLoading)

            // 3. Final state once the (fake) IO work completes.
            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            assertEquals("Milestone 2: architecture scaffold online.", loaded.statusMessage)
            assertEquals(null, loaded.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
