package com.sayanjalinexus.meshchat.ui.home

/**
 * Immutable UI state for [HomeScreen], produced by [HomeViewModel].
 *
 * This is intentionally minimal in Milestone 2 (the architecture-only
 * milestone). Real fields (peer count, connection status, unread message
 * count) are added as the repositories that back them land in later
 * milestones — the ViewModel/UiState/Screen wiring pattern established
 * here is reused unchanged for every subsequent screen.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val errorMessage: String? = null,
)
