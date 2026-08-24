package com.sayanjalinexus.meshchat.mesh

/**
 * Snapshot of mesh routing activity, matching most of the fields the REST
 * bridge's `GET /statistics` endpoint will expose once Milestone 8 lands
 * (see API.md). `peers_seen_total` from that contract is deliberately not
 * here — that's [com.sayanjalinexus.meshchat.data.PeerRepository]'s
 * concern (peer visibility), not the router's; the REST bridge composes
 * both sources when Milestone 8 builds the actual endpoint.
 */
data class RoutingStatistics(
    val packetsSent: Long = 0,
    val packetsRelayed: Long = 0,
    val packetsDroppedDuplicate: Long = 0,
    val packetsDroppedTtl: Long = 0,
    val packetsDroppedExpired: Long = 0,
    val averageHopCount: Double = 0.0,
)
