package com.sayanjalinexus.meshchat.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
        composeTestRule.setContent {
            MeshChatTheme {
                HomeScreenContent(
                    uiState = HomeUiState(
                        isLoading = false,
                        statusMessage = "Milestone 2: architecture scaffold online.",
                    ),
                )
            }
        }

        composeTestRule
            .onNodeWithText("Milestone 2: architecture scaffold online.")
            .assertExists()
    }

    @Test
    fun homeScreen_displaysErrorMessage_whenPresent() {
        composeTestRule.setContent {
            MeshChatTheme {
                HomeScreenContent(
                    uiState = HomeUiState(
                        isLoading = false,
                        statusMessage = "",
                        errorMessage = "Something went wrong",
                    ),
                )
            }
        }

        composeTestRule
            .onNodeWithText("Something went wrong")
            .assertExists()
    }
}
