package com.sayanjalinexus.meshchat.ble

/**
 * Events emitted by [BleAdvertiser.advertise]. Unlike [BleScanEvent], which
 * fires repeatedly (one per discovered peer), advertising only ever
 * produces a single terminal event per session: either it started
 * successfully, or it failed.
 */
sealed interface AdvertiseEvent {
    data object Started : AdvertiseEvent
    data class Failure(val reason: AdvertiseFailureReason) : AdvertiseEvent
}

/**
 * Platform-agnostic mapping of [android.bluetooth.le.AdvertiseCallback]'s
 * `ADVERTISE_FAILED_*` error codes, plus two reasons ([ADAPTER_UNAVAILABLE],
 * [UNKNOWN]) that originate from this app rather than the platform.
 */
enum class AdvertiseFailureReason(val nativeCode: Int) {
    DATA_TOO_LARGE(1),
    TOO_MANY_ADVERTISERS(2),
    ALREADY_STARTED(3),
    INTERNAL_ERROR(4),
    FEATURE_UNSUPPORTED(5),
    ADAPTER_UNAVAILABLE(-1),
    UNKNOWN(-2),
    ;

    companion object {
        fun fromNativeCode(code: Int): AdvertiseFailureReason =
            entries.firstOrNull { it.nativeCode == code } ?: UNKNOWN
    }
}
