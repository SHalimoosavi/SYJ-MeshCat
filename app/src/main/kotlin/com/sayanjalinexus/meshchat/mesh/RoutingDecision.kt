package com.sayanjalinexus.meshchat.mesh

/**
 * The outcome of [MeshRouter.handleIncomingPacket] for a single packet, per
 * PROTOCOL.md's routing rules. [MeshRouter] only decides; callers (the
 * transport layer, once it exists) are responsible for actually delivering
 * or relaying per the returned decision.
 */
sealed interface RoutingDecision {
    /** Addressed to this node (or a broadcast/channel packet) — deliver to the app layer. */
    data class DeliverLocal(val packet: MeshPacket) : RoutingDecision

    /** Forward [packet] (already TTL-decremented/hop-incremented) to every connected peer except [receivedFrom]. */
    data class Relay(val packet: MeshPacket, val receivedFrom: String) : RoutingDecision

    /** Already-seen message ID — silently dropped, no relay, no processing. */
    data object DropDuplicate : RoutingDecision

    /** TTL reached zero before the packet could be relayed further. */
    data object DropTtlExpired : RoutingDecision

    /** Packet's timestamp is outside the acceptable freshness window. */
    data object DropExpired : RoutingDecision
}
