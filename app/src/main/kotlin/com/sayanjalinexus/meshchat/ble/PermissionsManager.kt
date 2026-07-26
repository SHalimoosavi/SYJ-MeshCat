package com.sayanjalinexus.meshchat.ble

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central source of truth for which runtime permissions SYJ-MeshChat
 * needs, since Android's Bluetooth permission model has changed across API
 * levels:
 *  - API < 31: legacy `BLUETOOTH` + `BLUETOOTH_ADMIN` + `ACCESS_FINE_LOCATION`
 *    (BLE scanning required location access on these versions).
 *  - API 31+: granular `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` /
 *    `BLUETOOTH_ADVERTISE`, requested together here even though advertising
 *    itself isn't used until Milestone 4 — this avoids a second permission
 *    prompt later for a single logical "let this app use Bluetooth" grant.
 *  - API 33+: `POST_NOTIFICATIONS`, needed for the foreground scanning
 *    service's persistent notification.
 */
@Singleton
class PermissionsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun requiredPermissions(): Array<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    fun hasAllPermissions(): Boolean = missingPermissions().isEmpty()

    fun missingPermissions(): List<String> = requiredPermissions().filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }
}
