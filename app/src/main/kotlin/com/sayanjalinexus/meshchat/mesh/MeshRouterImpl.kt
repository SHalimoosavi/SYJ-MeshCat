package com.sayanjalinexus.meshchat.mesh

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Default [MeshRouter] implementation.
 *
 * [totalHopCount]/[hopSampleCount] use [AtomicLong] rather than plain
 * `var`s: [handleIncomingPacket] may eventually be called concurrently
 * from multiple simultaneous peer connections once the GATT transport
 * exists, and these two counters back the running average-hop-count
 * calculation, so they need to be safe under concurrent increment even
 * though the [statistics] StateFlow update itself is already atomic.
 */
@Singleton
class MeshRouterImpl @Inject constructor(
    private val seenPacketCache: SeenPacketCache,
) : MeshRouter {

    private val _statistics = MutableStateFlow(RoutingStatistics())
    override val statistics: StateFlow<RoutingStatistics> = _statistics.asStateFlow()

    private val totalHopCount = AtomicLong(0)
    private val hopSampleCount = AtomicLong(0)

    override fun handleIncomingPacket(
        packet: MeshPacket,
        receivedFrom: String,
        localIdentity: ByteArray,
    ): RoutingDecision {
        if (isExpired(packet)) {
            _statistics.update { it.copy(packetsDroppedExpired = it.packetsDroppedExpired + 1) }
            return RoutingDecision.DropExpired
        }

        val isNew = seenPacketCache.markSeenIfNew(packet.messageId)
        if (!isNew) {
            _statistics.update { it.copy(packetsDroppedDuplicate = it.packetsDroppedDuplicate + 1) }
            return RoutingDecision.DropDuplicate
        }

        recordHopCount(packet.hopCount)

        // isBroadcast (all-zero destination) stands in for "channel packet this
        // node subscribes to" — real per-channel subscription tracking arrives
        // with the Room database in Milestone 7; until then, every broadcast
        // packet is delivered locally.
        if (packet.isBroadcast || packet.destination.contentEquals(localIdentity)) {
            return RoutingDecision.DeliverLocal(packet)
        }

        if (packet.ttl <= 0) {
            _statistics.update { it.copy(packetsDroppedTtl = it.packetsDroppedTtl + 1) }
            return RoutingDecision.DropTtlExpired
        }

        val relayed = packet.copy(ttl = packet.ttl - 1, hopCount = packet.hopCount + 1)
        _statistics.update { it.copy(packetsRelayed = it.packetsRelayed + 1) }
        return RoutingDecision.Relay(relayed, receivedFrom)
    }

    override fun createOutgoingPacket(
        type: PacketType,
        sender: ByteArray,
        destination: ByteArray,
        payload: ByteArray,
        ttl: Int,
        flags: Int,
    ): MeshPacket {
        val packet = MeshPacket(
            version = MeshPacket.PROTOCOL_VERSION,
            type = type,
            flags = flags,
            ttl = ttl,
            hopCount = 0,
            sender = sender,
            destination = destination,
            messageId = UUID.randomUUID(),
            timestamp = System.currentTimeMillis(),
            payload = payload,
            // Zero-filled until Milestone 6 wires real Ed25519 signing.
            signature = ByteArray(MeshPacket.SIGNATURE_SIZE),
        )
        seenPacketCache.markSeenIfNew(packet.messageId)
        _statistics.update { it.copy(packetsSent = it.packetsSent + 1) }
        return packet
    }

    private fun isExpired(packet: MeshPacket): Boolean {
        val age = System.currentTimeMillis() - packet.timestamp
        return age > MAX_PACKET_AGE_MS || age < -CLOCK_SKEW_TOLERANCE_MS
    }

    private fun recordHopCount(hopCount: Int) {
        val total = totalHopCount.addAndGet(hopCount.toLong())
        val count = hopSampleCount.incrementAndGet()
        _statistics.update { it.copy(averageHopCount = total.toDouble() / count) }
    }

    companion object {
        private const val MAX_PACKET_AGE_MS = 10 * 60 * 1000L // 10 minutes
        private const val CLOCK_SKEW_TOLERANCE_MS = 30 * 1000L // tolerate a little future-dated clock skew
    }
}
