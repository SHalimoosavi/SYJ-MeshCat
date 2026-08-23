package com.sayanjalinexus.meshchat.data

import com.sayanjalinexus.meshchat.ble.AdvertiseState
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for this device's BLE advertising state — the
 * "be discoverable" counterpart to [PeerRepository]'s "discover others".
 */
interface AdvertisingRepository {
    val advertiseState: StateFlow<AdvertiseState>

    fun startAdvertising()
    fun stopAdvertising()
}
