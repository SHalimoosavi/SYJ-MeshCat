package com.sayanjalinexus.meshchat.data

import com.sayanjalinexus.meshchat.ble.BleScanState
import com.sayanjalinexus.meshchat.core.model.Peer
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for BLE-visible mesh peers. Wraps
 * [com.sayanjalinexus.meshchat.ble.BleTransport], adding permission/
 * availability checks, peer de-duplication, and stale-peer eviction.
 */
interface PeerRepository {
    val peers: StateFlow<List<Peer>>
    val scanState: StateFlow<BleScanState>

    fun startScanning()
    fun stopScanning()
}
