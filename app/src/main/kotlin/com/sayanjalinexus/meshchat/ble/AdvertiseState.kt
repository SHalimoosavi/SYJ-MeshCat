package com.sayanjalinexus.meshchat.ble

/**
 * High-level advertising state exposed to the UI layer via
 * [com.sayanjalinexus.meshchat.data.AdvertisingRepository.advertiseState] —
 * mirrors [BleScanState]'s shape for the advertising side of peer
 * discovery.
 */
sealed interface AdvertiseState {
    data object Idle : AdvertiseState
    data object Advertising : AdvertiseState
    data object Unsupported : AdvertiseState
    data object BluetoothDisabled : AdvertiseState
    data object PermissionsRequired : AdvertiseState
    data class Error(val reason: AdvertiseFailureReason) : AdvertiseState
}
