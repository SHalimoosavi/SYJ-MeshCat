package com.sayanjalinexus.meshchat.ble

import org.junit.Assert.assertEquals
import org.junit.Test

class BleScanFailureReasonTest {

    @Test
    fun `fromNativeCode maps every known ScanCallback error code`() {
        assertEquals(BleScanFailureReason.ALREADY_STARTED, BleScanFailureReason.fromNativeCode(1))
        assertEquals(BleScanFailureReason.APPLICATION_REGISTRATION_FAILED, BleScanFailureReason.fromNativeCode(2))
        assertEquals(BleScanFailureReason.INTERNAL_ERROR, BleScanFailureReason.fromNativeCode(3))
        assertEquals(BleScanFailureReason.FEATURE_UNSUPPORTED, BleScanFailureReason.fromNativeCode(4))
        assertEquals(BleScanFailureReason.OUT_OF_HARDWARE_RESOURCES, BleScanFailureReason.fromNativeCode(5))
        assertEquals(BleScanFailureReason.SCANNING_TOO_FREQUENTLY, BleScanFailureReason.fromNativeCode(6))
    }

    @Test
    fun `fromNativeCode falls back to UNKNOWN for unrecognized codes`() {
        assertEquals(BleScanFailureReason.UNKNOWN, BleScanFailureReason.fromNativeCode(999))
        assertEquals(BleScanFailureReason.UNKNOWN, BleScanFailureReason.fromNativeCode(0))
    }
}
