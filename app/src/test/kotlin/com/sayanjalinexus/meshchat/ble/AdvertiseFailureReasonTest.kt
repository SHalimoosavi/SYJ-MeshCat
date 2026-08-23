package com.sayanjalinexus.meshchat.ble

import org.junit.Assert.assertEquals
import org.junit.Test

class AdvertiseFailureReasonTest {

    @Test
    fun `fromNativeCode maps every known AdvertiseCallback error code`() {
        assertEquals(AdvertiseFailureReason.DATA_TOO_LARGE, AdvertiseFailureReason.fromNativeCode(1))
        assertEquals(AdvertiseFailureReason.TOO_MANY_ADVERTISERS, AdvertiseFailureReason.fromNativeCode(2))
        assertEquals(AdvertiseFailureReason.ALREADY_STARTED, AdvertiseFailureReason.fromNativeCode(3))
        assertEquals(AdvertiseFailureReason.INTERNAL_ERROR, AdvertiseFailureReason.fromNativeCode(4))
        assertEquals(AdvertiseFailureReason.FEATURE_UNSUPPORTED, AdvertiseFailureReason.fromNativeCode(5))
    }

    @Test
    fun `fromNativeCode falls back to UNKNOWN for unrecognized codes`() {
        assertEquals(AdvertiseFailureReason.UNKNOWN, AdvertiseFailureReason.fromNativeCode(999))
        assertEquals(AdvertiseFailureReason.UNKNOWN, AdvertiseFailureReason.fromNativeCode(0))
    }
}
