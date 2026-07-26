package com.sayanjalinexus.meshchat.ble

import com.sayanjalinexus.meshchat.core.model.Peer

/**
 * Events emitted by [BleTransport.scanResults]: either a discovered/updated
 * peer, or a scan failure.
 */
sealed interface BleScanEvent {
    data class Result(val peer: Peer) : BleScanEvent
    data class Failure(val reason: BleScanFailureReason) : BleScanEvent
}

/**
 * Platform-agnostic mapping of [android.bluetooth.le.ScanCallback]'s
 * `SCAN_FAILED_*` error codes, plus two reasons ([ADAPTER_UNAVAILABLE],
 * [UNKNOWN]) that originate from this app rather than the platform.
 */
enum class BleScanFailureReason(val nativeCode: Int) {
    ALREADY_STARTED(1),
    APPLICATION_REGISTRATION_FAILED(2),
    INTERNAL_ERROR(3),
    FEATURE_UNSUPPORTED(4),
    OUT_OF_HARDWARE_RESOURCES(5),
    SCANNING_TOO_FREQUENTLY(6),
    ADAPTER_UNAVAILABLE(-1),
    UNKNOWN(-2),
    ;

    companion object {
        fun fromNativeCode(code: Int): BleScanFailureReason =
            entries.firstOrNull { it.nativeCode == code } ?: UNKNOWN
    }
}
