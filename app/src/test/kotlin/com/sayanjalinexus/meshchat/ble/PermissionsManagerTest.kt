package com.sayanjalinexus.meshchat.ble

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [PermissionsManager.requiredPermissions] branches on `Build.VERSION.SDK_INT`,
 * which under the plain JVM `android.jar` stub (no Robolectric) reads as a
 * fixed low value — so these tests deliberately assert only on the
 * aggregate `hasAllPermissions`/`missingPermissions` behavior, which holds
 * regardless of which specific permission set is in play.
 */
class PermissionsManagerTest {

    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun `hasAllPermissions is true when every required permission is granted`() {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        val manager = PermissionsManager(context)

        assertTrue(manager.hasAllPermissions())
        assertTrue(manager.missingPermissions().isEmpty())
    }

    @Test
    fun `missingPermissions lists every permission that is not granted`() {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED

        val manager = PermissionsManager(context)

        assertFalse(manager.hasAllPermissions())
        assertEquals(manager.requiredPermissions().toList(), manager.missingPermissions())
    }

    @Test
    fun `requiredPermissions is never empty`() {
        val manager = PermissionsManager(context)

        assertTrue(manager.requiredPermissions().isNotEmpty())
    }
}
