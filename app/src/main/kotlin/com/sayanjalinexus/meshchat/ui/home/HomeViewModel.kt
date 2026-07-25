package com.sayanjalinexus.meshchat.ui.home

import com.sayanjalinexus.meshchat.core.BaseViewModel
import com.sayanjalinexus.meshchat.core.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for [HomeScreen].
 *
 * Establishes the pattern every future screen ViewModel follows:
 * - private [MutableStateFlow] backing a public read-only [StateFlow],
 * - Hilt constructor injection ([DispatcherProvider] today; repositories
 *   are added as they're built in later milestones),
 * - work funneled through [launchSafely] from [BaseViewModel] so errors
 *   land in [HomeUiState.errorMessage] instead of crashing the app.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStatus()
    }

    private fun loadStatus() {
        launchSafely {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Placeholder for real status once MeshRepository exists
            // (Milestone 7/8). Runs on the IO dispatcher to establish the
            // convention even though there's no I/O yet.
            val status = withContext(dispatcherProvider.io) {
                "Milestone 2: architecture scaffold online."
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = status,
            )
        }
    }

    override fun onUnhandledError(throwable: Throwable) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = throwable.message ?: "Unknown error",
        )
    }
}
