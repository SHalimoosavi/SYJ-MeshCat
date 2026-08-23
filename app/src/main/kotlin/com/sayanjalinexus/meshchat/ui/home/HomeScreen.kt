@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.sayanjalinexus.meshchat.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.sayanjalinexus.meshchat.ble.AdvertiseState
import com.sayanjalinexus.meshchat.ble.BleScanState
import com.sayanjalinexus.meshchat.core.model.Peer
import com.sayanjalinexus.meshchat.ui.theme.MeshChatTheme

/**
 * Entry screen of the app. Stateful overload wires up Hilt, the
 * ViewModel's [StateFlow][kotlinx.coroutines.flow.StateFlow], and the
 * runtime permission request flow needed before BLE scanning/advertising
 * can start. The stateless overload below ([HomeScreenContent]) renders
 * pure UI from a [HomeUiState] and is what gets exercised in Compose
 * previews and UI tests (see `HomeScreenTest`).
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grantResults ->
        viewModel.onPermissionsResult(allGranted = grantResults.values.all { it })
    }

    HomeScreenContent(
        uiState = uiState,
        onToggleScanClick = {
            if (viewModel.hasRequiredPermissions()) {
                viewModel.onToggleScanRequested()
            } else {
                permissionLauncher.launch(viewModel.requiredPermissions())
            }
        },
    )
}

@Composable
internal fun HomeScreenContent(uiState: HomeUiState, onToggleScanClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.home_title)) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                text = bleScanStateLabel(uiState.bleScanState),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = advertiseStateLabel(uiState.advertiseState),
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

            Button(onClick = onToggleScanClick, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (uiState.bleScanState == BleScanState.Scanning) {
                        stringResource(R.string.scan_button_stop)
                    } else {
                        stringResource(R.string.scan_button_start)
                    },
                )
            }

            HorizontalDivider()

            Text(
                text = stringResource(R.string.peers_list_title),
                style = MaterialTheme.typography.titleMedium,
            )

            if (uiState.peers.isEmpty()) {
                Text(
                    text = stringResource(R.string.peers_list_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = uiState.peers, key = { it.address }) { peer ->
                        PeerRow(peer)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerRow(peer: Peer) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = peer.nickname ?: stringResource(R.string.peer_unknown_nickname),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.peer_rssi_format, peer.address, peer.rssi),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun bleScanStateLabel(state: BleScanState): String = when (state) {
    is BleScanState.Idle -> stringResource(R.string.ble_state_idle)
    is BleScanState.Scanning -> stringResource(R.string.ble_state_scanning)
    is BleScanState.Unsupported -> stringResource(R.string.ble_state_unsupported)
    is BleScanState.BluetoothDisabled -> stringResource(R.string.ble_state_bluetooth_disabled)
    is BleScanState.PermissionsRequired -> stringResource(R.string.ble_state_permissions_required)
    is BleScanState.Error -> stringResource(R.string.ble_state_error)
}

@Composable
private fun advertiseStateLabel(state: AdvertiseState): String = when (state) {
    is AdvertiseState.Idle -> stringResource(R.string.advertise_state_idle)
    is AdvertiseState.Advertising -> stringResource(R.string.advertise_state_advertising)
    is AdvertiseState.Unsupported -> stringResource(R.string.advertise_state_unsupported)
    is AdvertiseState.BluetoothDisabled -> stringResource(R.string.advertise_state_bluetooth_disabled)
    is AdvertiseState.PermissionsRequired -> stringResource(R.string.advertise_state_permissions_required)
    is AdvertiseState.Error -> stringResource(R.string.advertise_state_error)
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MeshChatTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                statusMessage = "Milestone 4: BLE advertising online.",
                bleScanState = BleScanState.Scanning,
                advertiseState = AdvertiseState.Advertising,
                peers = listOf(
                    Peer(address = "AA:BB:CC:DD:EE:01", nickname = "node-1", rssi = -52, lastSeenAt = 0L),
                    Peer(address = "AA:BB:CC:DD:EE:02", nickname = null, rssi = -71, lastSeenAt = 0L),
                ),
            ),
        )
    }
}
