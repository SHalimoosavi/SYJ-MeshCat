package com.sayanjalinexus.meshchat.ui.home

import com.sayanjalinexus.meshchat.ble.BleScanState
import com.sayanjalinexus.meshchat.ble.PermissionsManager
import com.sayanjalinexus.meshchat.core.BaseViewModel
import com.sayanjalinexus.meshchat.core.DispatcherProvider
import com.sayanjalinexus.meshchat.data.PeerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for [HomeScreen].
 *
 * Establishes the pattern every future screen ViewModel follows:
 * - private [MutableStateFlow] backing a public read-only [StateFlow],
 * - Hilt constructor injection ([DispatcherProvider], [PeerRepository],
 *   [PermissionsManager]),
 * - work funneled through [launchSafely] from [BaseViewModel] so errors
 *   land in [HomeUiState.errorMessage] instead of crashing the app.
 *
 * BLE scanning itself (start/stop, permission gating) is delegated to
 * [PeerRepository] — this ViewModel only reflects its state into
 * [HomeUiState] and forwards user intent (toggling the scan button).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val peerRepository: PeerRepository,
    private val permissionsManager: PermissionsManager,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStatus()
        observeMeshState()
    }

    /** Permissions the UI must request before scanning can start. */
    fun requiredPermissions(): Array<String> = permissionsManager.requiredPermissions()

    fun hasRequiredPermissions(): Boolean = permissionsManager.hasAllPermissions()

    /** Called by the UI after the system permission dialog is dismissed. */
    fun onPermissionsResult(allGranted: Boolean) {
        if (allGranted) {
            startScanning()
        } else {
            _uiState.update { it.copy(bleScanState = BleScanState.PermissionsRequired) }
        }
    }

    fun onToggleScanRequested() {
        if (_uiState.value.bleScanState == BleScanState.Scanning) {
            peerRepository.stopScanning()
        } else {
            startScanning()
        }
    }

    private fun startScanning() {
        if (!permissionsManager.hasAllPermissions()) {
            _uiState.update { it.copy(bleScanState = BleScanState.PermissionsRequired) }
            return
        }
        peerRepository.startScanning()
    }

    private fun observeMeshState() {
        launchSafely {
            combine(peerRepository.peers, peerRepository.scanState) { peers, scanState ->
                peers to scanState
            }.collect { (peers, scanState) ->
                _uiState.update { it.copy(peers = peers, bleScanState = scanState) }
            }
        }
    }

    private fun loadStatus() {
        launchSafely {
            _uiState.update { it.copy(isLoading = true) }

            // Placeholder for real status once a MeshRepository (routing/
            // encryption state) exists in later milestones. Runs on the IO
            // dispatcher to establish the convention even though there's no
            // I/O yet.
            val status = withContext(dispatcherProvider.io) {
                "Milestone 3: BLE scanning online."
            }

            _uiState.update { it.copy(isLoading = false, statusMessage = status) }
        }
    }

    override fun onUnhandledError(throwable: Throwable) {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = throwable.message ?: "Unknown error")
        }
    }
}
