package com.sayanjalinexus.meshchat.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for Navigation Compose 2.8+.
 *
 * Only [Home] exists as of Milestone 2. Additional destinations (Chat,
 * PeerList, Statistics, Settings) are added in Milestone 10 alongside
 * their screens — declared here first so [MeshNavHost] has a single,
 * growing source of truth for the app's navigation graph.
 */
sealed interface MeshRoute {
    @Serializable
    data object Home : MeshRoute
}
