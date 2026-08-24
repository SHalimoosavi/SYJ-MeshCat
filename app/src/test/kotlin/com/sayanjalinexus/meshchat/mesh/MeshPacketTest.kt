package com.sayanjalinexus.meshchat.mesh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

class MeshPacketTest {

    private fun randomIdentity(): ByteArray = Random.nextBytes(MeshPacket.IDENTITY_SIZE)
    private fun zeroIdentity(): ByteArray = ByteArray(MeshPacket.IDENTITY_SIZE)
    private fun zeroSignature(): ByteArray = ByteArray(MeshPacket.SIGNATURE_SIZE)

    private fun samplePacket(payload: ByteArray = "hello mesh".toByteArray()) = MeshPacket(
        version = MeshPacket.PROTOCOL_VERSION,
        type = PacketType.PRIVATE_MESSAGE,
        flags = MeshFlags.ENCRYPTED,
        ttl = 5,
        hopCount = 0,
        sender = randomIdentity(),
        destination = randomIdentity(),
        messageId = UUID.randomUUID(),
        timestamp = System.currentTimeMillis(),
        payload = payload,
        signature = zeroSignature(),
    )

    @Test
    fun `encode then decode round-trips to an equal packet`() {
        val original = samplePacket()

        val decoded = MeshPacket.decode(MeshPacket.encode(original))

        assertEquals(original, decoded)
    }

    @Test
    fun `round-trip works with empty payload`() {
        val original = samplePacket(payload = ByteArray(0))

        val decoded = MeshPacket.decode(MeshPacket.encode(original))

        assertEquals(original, decoded)
    }

    @Test
    fun `round-trip works with a large payload near the size limit`() {
        val original = samplePacket(payload = Random.nextBytes(4096))

        val decoded = MeshPacket.decode(MeshPacket.encode(original))

        assertEquals(original, decoded)
    }

    @Test
    fun `broadcast destination is detected correctly`() {
        val broadcastPacket = samplePacket().copy(destination = zeroIdentity())
        val directedPacket = samplePacket()

        assertTrue(broadcastPacket.isBroadcast)
        assertTrue(!directedPacket.isBroadcast || directedPacket.destination.all { it == 0.toByte() })
    }

    @Test
    fun `decode rejects a truncated byte array`() {
        val bytes = MeshPacket.encode(samplePacket())
        val truncated = bytes.copyOfRange(0, bytes.size - 10)

        assertNull(MeshPacket.decode(truncated))
    }

    @Test
    fun `decode rejects corrupted CRC`() {
        val bytes = MeshPacket.encode(samplePacket())
        // Flip a bit in the payload region, well before the CRC itself,
        // so the corruption is only caught by the CRC check.
        bytes[MeshPacket.FIXED_HEADER_SIZE] = bytes[MeshPacket.FIXED_HEADER_SIZE].inc()

        assertNull(MeshPacket.decode(bytes))
    }

    @Test
    fun `decode rejects an unrecognized packet type code`() {
        val bytes = MeshPacket.encode(samplePacket())
        // Byte 1 is the Type field — corrupt it to an unused code, then the
        // CRC itself must also be recomputed so this test isolates the
        // "unknown type" rejection path rather than tripping the CRC check.
        val mutable = bytes.copyOf()
        mutable[1] = 0x7F // not a defined PacketType code

        val crc = java.util.zip.CRC32()
        crc.update(mutable, 0, mutable.size - MeshPacket.CRC_SIZE)
        val crcBytes = java.nio.ByteBuffer.allocate(4).putInt(crc.value.toInt()).array()
        crcBytes.copyInto(mutable, mutable.size - MeshPacket.CRC_SIZE)

        assertNull(MeshPacket.decode(mutable))
    }

    @Test
    fun `packets with identical content but different array instances are equal`() {
        val sharedMessageId = UUID.randomUUID()
        val senderBytes = randomIdentity()
        val destinationBytes = randomIdentity()
        val payloadBytes = "same content".toByteArray()

        val a = MeshPacket(
            version = 1,
            type = PacketType.ACK,
            flags = 0,
            ttl = 3,
            hopCount = 1,
            sender = senderBytes.copyOf(),
            destination = destinationBytes.copyOf(),
            messageId = sharedMessageId,
            timestamp = 1000L,
            payload = payloadBytes.copyOf(),
            signature = zeroSignature(),
        )
        val b = a.copy(
            sender = senderBytes.copyOf(),
            destination = destinationBytes.copyOf(),
            payload = payloadBytes.copyOf(),
            signature = zeroSignature(),
        )

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `constructor rejects wrong-size identity fields`() {
        var threw = false
        try {
            samplePacket().copy(sender = ByteArray(10))
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertTrue(threw)
    }
}
