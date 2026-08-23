package com.sayanjalinexus.meshchat.ui.home

import com.sayanjalinexus.meshchat.ble.AdvertiseState
import com.sayanjalinexus.meshchat.ble.BleScanState
import com.sayanjalinexus.meshchat.core.model.Peer

/**
 * Immutable UI state for [HomeScreen], produced by [HomeViewModel].
 *
 * Grows one field per subsystem as it lands: [bleScanState] and [peers]
 * arrived in Milestone 3 (BLE scanning); [advertiseState] arrived in
 * Milestone 4 (BLE advertising). The ViewModel/UiState/Screen wiring
 * pattern established in Milestone 2 is reused unchanged for every
 * subsequent addition.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val errorMessage: String? = null,
    val bleScanState: BleScanState = BleScanState.Idle,
    val peers: List<Peer> = emptyList(),
    val advertiseState: AdvertiseState = AdvertiseState.Idle,
)
