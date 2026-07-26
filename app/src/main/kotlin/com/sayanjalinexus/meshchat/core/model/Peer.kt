package com.sayanjalinexus.meshchat.core.model

/**
 * A mesh node currently visible over BLE — i.e. a live scan result, not yet
 * a persisted identity.
 *
 * This is intentionally distinct from the Room `Peer` entity added in
 * Milestone 7, which tracks known identities across app restarts
 * (nickname, trust/quality score, routing history) regardless of whether
 * that peer is in radio range right now. This [Peer] only exists while the
 * device is actually hearing BLE advertisements from it.
 */
data class Peer(
    val address: String,
    val nickname: String?,
    val rssi: Int,
    val lastSeenAt: Long,
)
