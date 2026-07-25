package com.sayanjalinexus.meshchat.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayanjalinexus.meshchat.R
import com.sayanjalinexus.meshchat.ui.theme.MeshChatTheme

/**
 * Entry screen of the app. Stateful overload wires up Hilt + the
 * ViewModel's [StateFlow][kotlinx.coroutines.flow.StateFlow]; the stateless
 * overload below renders pure UI from a [HomeUiState] and is what gets
 * exercised in Compose previews and UI tests.
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(uiState = uiState)
}

@Composable
internal fun HomeScreenContent(uiState: HomeUiState) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.home_title)) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            if (uiState.statusMessage.isNotBlank()) {
                Text(
                    text = uiState.statusMessage,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Text(
                text = stringResource(R.string.home_scaffold_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MeshChatTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                statusMessage = "Milestone 2: architecture scaffold online.",
            ),
        )
    }
}
