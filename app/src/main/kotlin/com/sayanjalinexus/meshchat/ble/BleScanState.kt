package com.sayanjalinexus.meshchat.ble

/**
 * High-level scanning state exposed to the UI layer via
 * [com.sayanjalinexus.meshchat.data.PeerRepository.scanState] — distinct
 * from [BleScanEvent], which is the lower-level per-callback event stream
 * from the transport.
 */
sealed interface BleScanState {
    data object Idle : BleScanState
    data object Scanning : BleScanState
    data object Unsupported : BleScanState
    data object BluetoothDisabled : BleScanState
    data object PermissionsRequired : BleScanState
    data class Error(val reason: BleScanFailureReason) : BleScanState
}
