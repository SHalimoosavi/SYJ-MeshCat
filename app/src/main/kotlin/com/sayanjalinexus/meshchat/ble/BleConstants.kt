package com.sayanjalinexus.meshchat.ble

import java.util.UUID

/**
 * Shared BLE constants for peer discovery.
 */
object BleConstants {

    /**
     * GATT service UUID advertised by every SYJ-MeshChat node once
     * BLE advertising lands in Milestone 4, and used to filter scan
     * results here in Milestone 3. This is a custom, randomly generated
     * 128-bit UUID — original to this project, not derived from any
     * proprietary or Bluetooth SIG–reserved UUID.
     */
    val MESH_SERVICE_UUID: UUID = UUID.fromString("8e7f1a20-4c9b-4a2e-9d3f-6a1b2c3d4e5f")

    /** Batch scan results immediately rather than delaying delivery. */
    const val SCAN_REPORT_DELAY_MS = 0L

    /** How long a peer can go unseen before it's evicted from the visible-peers list. */
    const val PEER_STALE_TIMEOUT_MS = 30_000L

    /** How often the eviction loop checks for stale peers. */
    const val PEER_EVICTION_INTERVAL_MS = 10_000L
}
