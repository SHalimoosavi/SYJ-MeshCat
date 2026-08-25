package com.sayanjalinexus.meshchat.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sayanjalinexus.meshchat.R
import com.sayanjalinexus.meshchat.ble.AdvertiseState
import com.sayanjalinexus.meshchat.ble.BleScanState
import com.sayanjalinexus.meshchat.core.model.Peer
import com.sayanjalinexus.meshchat.ui.theme.MeshChatTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumentation test for the stateless [HomeScreen] content, exercising
 * real Compose measurement/layout/semantics on a device or emulator
 * (unlike [HomeViewModelTest], which is a pure-JVM unit test).
 */
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysLoadedStatusMessage() {
        val uiState =
            HomeUiState(
                isLoading = false,
                statusMessage = "Milestone 4: BLE advertising online.",
            )

        composeTestRule.setContent {
            MeshChatTheme {
                HomeScreenContent(uiState = uiState)
            }
        }

        composeTestRule
            .onNodeWithText("Milestone 4: BLE advertising online.")
            .assertExists()
    }

    @Test
    fun homeScreen_displaysErrorMessage_whenPresent() {
        val uiState =
            HomeUiState(
                isLoading = false,
                statusMessage = "",
                errorMessage = "Something went wrong",
            )

        composeTestRule.setContent {
            MeshChatTheme {
                HomeScreenContent(uiState = uiState)
            }
        }

        composeTestRule
            .onNodeWithText("Something went wrong")
            .assertExists()
    }

    @Test
    fun homeScreen_displaysEmptyPeersMessage_whenNoPeersDiscovered() {
        val uiState = HomeUiState(peers = emptyList())

        composeTestRule.setContent {
            MeshChatTheme {
                HomeScreenContent(uiState = uiState)
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.peers_list_empty))
            .assertExists()
    }

    @Test
    fun homeScreen_displaysDiscoveredPeerNickname() {
        val peer = Peer(address = "AA:BB:CC:DD:EE:FF", nickname = "node-1", rssi = -55, lastSeenAt = 0L)
        val uiState = HomeUiState(bleScanState = BleScanState.Scanning, peers = listOf(peer))

        composeTestRule.setContent {
            MeshChatTheme {
                HomeScreenContent(uiState = uiState)
            }
        }

        composeTestRule.onNodeWithText("node-1").assertExists()
    }

    @Test
    fun homeScreen_displaysAdvertisingLabel_whenDiscoverable() {
        val uiState = HomeUiState(advertiseState = AdvertiseState.Advertising)

        composeTestRule.setContent {
            MeshChatTheme {
                HomeScreenContent(uiState = uiState)
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.advertise_state_advertising))
            .assertExists()
    }

    @Test
    fun homeScreen_scanButtonClick_invokesCallback() {
        var clicked = false
        val uiState = HomeUiState(bleScanState = BleScanState.Idle)

        composeTestRule.setContent {
            MeshChatTheme {
                HomeScreenContent(
                    uiState = uiState,
                    onToggleScanClick = { clicked = true },
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.scan_button_start))
            .performClick()

        assert(clicked) { "Expected onToggleScanClick to be invoked after tapping the scan button" }
    }
}
