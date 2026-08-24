package com.sayanjalinexus.meshchat.mesh

import kotlinx.coroutines.flow.StateFlow

/**
 * Applies PROTOCOL.md's routing rules to mesh traffic: duplicate
 * suppression, TTL-bounded relay, and local-delivery classification.
 *
 * This is purely decision logic — it performs no I/O and knows nothing
 * about BLE. The actual GATT-based transport that feeds packets in and
 * carries [RoutingDecision.Relay] results back out over the air is a
 * separate concern, layered on top of this router once it exists.
 */
interface MeshRouter {
    val statistics: StateFlow<RoutingStatistics>

    /**
     * Classifies an incoming packet per PROTOCOL.md's routing rules:
     * reject if expired, drop if a duplicate (by message ID), deliver
     * locally if [localIdentity] matches the destination (or the packet is
     * a broadcast), otherwise relay (TTL-decremented, hop-incremented) or
     * drop if TTL has been exhausted.
     *
     * Packet *signature verification* (PROTOCOL.md routing rule #1) is not
     * performed here — there is no real signing yet until Milestone 6's
     * crypto layer exists, so [MeshPacket.signature] is presently a
     * zero-filled placeholder. This will be added as a precondition once
     * Milestone 6 lands, without changing this method's shape.
     */
    fun handleIncomingPacket(
        packet: MeshPacket,
        receivedFrom: String,
        localIdentity: ByteArray,
    ): RoutingDecision

    /**
     * Builds a fresh outgoing [MeshPacket] (new message ID, current
     * timestamp, hop count 0) and records it in [statistics]. Also
     * pre-marks the new message ID as seen, so that if this packet loops
     * back to us via mesh relay, it's recognized as a duplicate rather
     * than being reprocessed or re-relayed. Does not transmit it — the
     * caller hands the result to the transport layer.
     */
    fun createOutgoingPacket(
        type: PacketType,
        sender: ByteArray,
        destination: ByteArray,
        payload: ByteArray,
        ttl: Int = DEFAULT_TTL,
        flags: Int = 0,
    ): MeshPacket

    companion object {
        const val DEFAULT_TTL = 5
    }
}
