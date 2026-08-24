package com.sayanjalinexus.meshchat.mesh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

class MeshRouterImplTest {

    private lateinit var router: MeshRouterImpl

    private fun randomIdentity(): ByteArray = Random.nextBytes(MeshPacket.IDENTITY_SIZE)
    private fun zeroIdentity(): ByteArray = ByteArray(MeshPacket.IDENTITY_SIZE)

    @Before
    fun setUp() {
        router = MeshRouterImpl(seenPacketCache = SeenPacketCache())
    }

    private fun packet(
        destination: ByteArray = randomIdentity(),
        ttl: Int = 5,
        hopCount: Int = 0,
        timestamp: Long = System.currentTimeMillis(),
        messageId: UUID = UUID.randomUUID(),
    ) = MeshPacket(
        version = MeshPacket.PROTOCOL_VERSION,
        type = PacketType.PRIVATE_MESSAGE,
        flags = 0,
        ttl = ttl,
        hopCount = hopCount,
        sender = randomIdentity(),
        destination = destination,
        messageId = messageId,
        timestamp = timestamp,
        payload = "test".toByteArray(),
        signature = ByteArray(MeshPacket.SIGNATURE_SIZE),
    )

    @Test
    fun `packet addressed to this node is delivered locally`() {
        val myIdentity = randomIdentity()
        val incoming = packet(destination = myIdentity)

        val decision = router.handleIncomingPacket(incoming, receivedFrom = "peer-1", localIdentity = myIdentity)

        assertTrue(decision is RoutingDecision.DeliverLocal)
        assertEquals(incoming, (decision as RoutingDecision.DeliverLocal).packet)
    }

    @Test
    fun `broadcast packet is delivered locally regardless of local identity`() {
        val incoming = packet(destination = zeroIdentity())

        val decision = router.handleIncomingPacket(incoming, receivedFrom = "peer-1", localIdentity = randomIdentity())

        assertTrue(decision is RoutingDecision.DeliverLocal)
    }

    @Test
    fun `packet for someone else with TTL remaining is relayed with decremented TTL and incremented hop count`() {
        val incoming = packet(destination = randomIdentity(), ttl = 5, hopCount = 2)

        val decision = router.handleIncomingPacket(incoming, receivedFrom = "peer-1", localIdentity = randomIdentity())

        assertTrue(decision is RoutingDecision.Relay)
        val relay = decision as RoutingDecision.Relay
        assertEquals(4, relay.packet.ttl)
        assertEquals(3, relay.packet.hopCount)
        assertEquals("peer-1", relay.receivedFrom)
    }

    @Test
    fun `packet for someone else with TTL exhausted is dropped, not relayed`() {
        val incoming = packet(destination = randomIdentity(), ttl = 0)

        val decision = router.handleIncomingPacket(incoming, receivedFrom = "peer-1", localIdentity = randomIdentity())

        assertEquals(RoutingDecision.DropTtlExpired, decision)
        assertEquals(1, router.statistics.value.packetsDroppedTtl)
    }

    @Test
    fun `duplicate message id is dropped on second sighting`() {
        val sharedId = UUID.randomUUID()
        val first = packet(destination = randomIdentity(), messageId = sharedId)
        val second = first.copy(hopCount = first.hopCount + 1)
        val myIdentity = randomIdentity()

        val firstDecision = router.handleIncomingPacket(first, receivedFrom = "peer-1", localIdentity = myIdentity)
        val secondDecision = router.handleIncomingPacket(second, receivedFrom = "peer-2", localIdentity = myIdentity)

        assertTrue(firstDecision !is RoutingDecision.DropDuplicate)
        assertEquals(RoutingDecision.DropDuplicate, secondDecision)
        assertEquals(1, router.statistics.value.packetsDroppedDuplicate)
    }

    @Test
    fun `expired packet is dropped before duplicate or TTL checks`() {
        val ancientTimestamp = System.currentTimeMillis() - (60 * 60 * 1000L) // 1 hour old
        val incoming = packet(timestamp = ancientTimestamp)

        val decision = router.handleIncomingPacket(incoming, receivedFrom = "peer-1", localIdentity = randomIdentity())

        assertEquals(RoutingDecision.DropExpired, decision)
        assertEquals(1, router.statistics.value.packetsDroppedExpired)
    }

    @Test
    fun `future-dated packet within clock skew tolerance is accepted`() {
        val slightlyFuture = System.currentTimeMillis() + 5000L // 5 seconds ahead
        val myIdentity = randomIdentity()
        val incoming = packet(destination = myIdentity, timestamp = slightlyFuture)

        val decision = router.handleIncomingPacket(incoming, receivedFrom = "peer-1", localIdentity = myIdentity)

        assertTrue(decision is RoutingDecision.DeliverLocal)
    }

    @Test
    fun `createOutgoingPacket increments packetsSent and produces a valid packet`() {
        val sender = randomIdentity()
        val destination = randomIdentity()

        val outgoing = router.createOutgoingPacket(
            type = PacketType.PRIVATE_MESSAGE,
            sender = sender,
            destination = destination,
            payload = "hi".toByteArray(),
        )

        assertEquals(1, router.statistics.value.packetsSent)
        assertEquals(0, outgoing.hopCount)
        assertEquals(MeshRouter.DEFAULT_TTL, outgoing.ttl)
        assertTrue(outgoing.sender.contentEquals(sender))
    }

    @Test
    fun `a packet this node originated is recognized as a duplicate if it loops back`() {
        val myIdentity = randomIdentity()
        val outgoing = router.createOutgoingPacket(
            type = PacketType.CHANNEL_MESSAGE,
            sender = myIdentity,
            destination = zeroIdentity(),
            payload = "broadcast".toByteArray(),
        )

        // Simulate the packet coming back to us after being relayed by a peer.
        val loopedBack = outgoing.copy(ttl = outgoing.ttl - 1, hopCount = outgoing.hopCount + 1)
        val decision = router.handleIncomingPacket(loopedBack, receivedFrom = "peer-1", localIdentity = myIdentity)

        assertEquals(RoutingDecision.DropDuplicate, decision)
    }

    @Test
    fun `average hop count reflects processed packets`() {
        val myIdentity = randomIdentity()
        router.handleIncomingPacket(packet(destination = myIdentity, hopCount = 2), "peer-1", myIdentity)
        router.handleIncomingPacket(packet(destination = myIdentity, hopCount = 4), "peer-1", myIdentity)

        assertEquals(3.0, router.statistics.value.averageHopCount, 0.0001)
    }
}
