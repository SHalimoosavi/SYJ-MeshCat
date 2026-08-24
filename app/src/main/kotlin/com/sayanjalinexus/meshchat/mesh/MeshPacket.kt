package com.sayanjalinexus.meshchat.mesh

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.zip.CRC32

/**
 * A single mesh packet, per PROTOCOL.md's wire format:
 *
 * ```
 * Version(1) | Type(1) | Flags(1) | TTL(1) | HopCount(1) | Sender(32) |
 * Destination(32) | MessageId(16) | Timestamp(8) | PayloadLength(2) |
 * Payload(var) | Signature(64) | CRC(4)
 * ```
 *
 * [sender]/[destination] are 32-byte identity slots sized for a future
 * Ed25519 public key (Milestone 6). Until then they hold whatever
 * transient/placeholder identity bytes the caller supplies — [MeshRouter]
 * and this class don't care what's actually inside them, only that they're
 * the right size. Likewise [signature] is present in the wire format now
 * (so the byte layout doesn't change later) but is zero-filled until
 * Milestone 6 wires real signing; [MeshRouter] does not verify it yet.
 *
 * Overrides [equals]/[hashCode] manually rather than relying on the
 * `data class`-generated versions, because Kotlin's auto-generated
 * equals/hashCode use *referential* equality for [ByteArray] properties,
 * not content equality — without this override, two packets with
 * byte-for-byte identical content but different array instances would
 * incorrectly compare as unequal.
 */
data class MeshPacket(
    val version: Int,
    val type: PacketType,
    val flags: Int,
    val ttl: Int,
    val hopCount: Int,
    val sender: ByteArray,
    val destination: ByteArray,
    val messageId: UUID,
    val timestamp: Long,
    val payload: ByteArray,
    val signature: ByteArray,
) {
    init {
        require(sender.size == IDENTITY_SIZE) { "sender must be $IDENTITY_SIZE bytes, was ${sender.size}" }
        require(destination.size == IDENTITY_SIZE) { "destination must be $IDENTITY_SIZE bytes, was ${destination.size}" }
        require(signature.size == SIGNATURE_SIZE) { "signature must be $SIGNATURE_SIZE bytes, was ${signature.size}" }
        require(payload.size <= MAX_PAYLOAD_SIZE) { "payload exceeds $MAX_PAYLOAD_SIZE bytes: ${payload.size}" }
        require(version in 0..UBYTE_MAX) { "version out of range: $version" }
        require(ttl in 0..UBYTE_MAX) { "ttl out of range: $ttl" }
        require(hopCount in 0..UBYTE_MAX) { "hopCount out of range: $hopCount" }
    }

    /** All-zero destination is this protocol's broadcast/channel marker (see PROTOCOL.md). */
    val isBroadcast: Boolean get() = destination.all { it == 0.toByte() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MeshPacket) return false
        return version == other.version &&
            type == other.type &&
            flags == other.flags &&
            ttl == other.ttl &&
            hopCount == other.hopCount &&
            sender.contentEquals(other.sender) &&
            destination.contentEquals(other.destination) &&
            messageId == other.messageId &&
            timestamp == other.timestamp &&
            payload.contentEquals(other.payload) &&
            signature.contentEquals(other.signature)
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + type.hashCode()
        result = 31 * result + flags
        result = 31 * result + ttl
        result = 31 * result + hopCount
        result = 31 * result + sender.contentHashCode()
        result = 31 * result + destination.contentHashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }

    override fun toString(): String =
        "MeshPacket(version=$version, type=$type, flags=$flags, ttl=$ttl, hopCount=$hopCount, " +
            "messageId=$messageId, timestamp=$timestamp, payloadSize=${payload.size})"

    companion object {
        const val PROTOCOL_VERSION = 1

        const val IDENTITY_SIZE = 32
        const val MESSAGE_ID_SIZE = 16
        const val SIGNATURE_SIZE = 64
        const val CRC_SIZE = 4

        /** Version+Type+Flags+TTL+HopCount+Sender+Destination+MessageId+Timestamp+PayloadLength. */
        const val FIXED_HEADER_SIZE = 1 + 1 + 1 + 1 + 1 + IDENTITY_SIZE + IDENTITY_SIZE +
            MESSAGE_ID_SIZE + 8 + 2

        /** Total non-payload overhead: [FIXED_HEADER_SIZE] + [SIGNATURE_SIZE] + [CRC_SIZE]. */
        const val FIXED_OVERHEAD = FIXED_HEADER_SIZE + SIGNATURE_SIZE + CRC_SIZE

        const val MAX_PAYLOAD_SIZE = 65_535
        private const val UBYTE_MAX = 255

        /** Serializes [packet] to its wire-format byte representation, including a fresh CRC32. */
        fun encode(packet: MeshPacket): ByteArray {
            val totalSize = FIXED_HEADER_SIZE + packet.payload.size + SIGNATURE_SIZE + CRC_SIZE
            val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.BIG_ENDIAN)

            buffer.put(packet.version.toByte())
            buffer.put(packet.type.code.toByte())
            buffer.put(packet.flags.toByte())
            buffer.put(packet.ttl.toByte())
            buffer.put(packet.hopCount.toByte())
            buffer.put(packet.sender)
            buffer.put(packet.destination)
            buffer.put(uuidToBytes(packet.messageId))
            buffer.putLong(packet.timestamp)
            buffer.putShort(packet.payload.size.toShort())
            buffer.put(packet.payload)
            buffer.put(packet.signature)

            val bodyLength = totalSize - CRC_SIZE
            val crc = CRC32()
            crc.update(buffer.array(), 0, bodyLength)
            buffer.putInt(crc.value.toInt())

            return buffer.array()
        }

        /**
         * Parses [bytes] back into a [MeshPacket]. Returns `null` (rather than
         * throwing) for any malformed input: too short, length mismatch, CRC
         * failure, or an unrecognized [PacketType] code — per PROTOCOL.md's
         * routing rule #1, an unverifiable packet must never be processed or
         * relayed, and a clean `null` lets callers treat "reject" uniformly
         * without a try/catch at every call site.
         */
        fun decode(bytes: ByteArray): MeshPacket? {
            val minSize = FIXED_HEADER_SIZE + SIGNATURE_SIZE + CRC_SIZE
            if (bytes.size < minSize) return null

            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

            val version = buffer.get().toInt() and 0xFF
            val typeCode = buffer.get().toInt() and 0xFF
            val flags = buffer.get().toInt() and 0xFF
            val ttl = buffer.get().toInt() and 0xFF
            val hopCount = buffer.get().toInt() and 0xFF

            val sender = ByteArray(IDENTITY_SIZE).also { buffer.get(it) }
            val destination = ByteArray(IDENTITY_SIZE).also { buffer.get(it) }
            val messageIdBytes = ByteArray(MESSAGE_ID_SIZE).also { buffer.get(it) }
            val timestamp = buffer.long
            val payloadLength = buffer.short.toInt() and 0xFFFF

            val expectedTotalSize = FIXED_HEADER_SIZE + payloadLength + SIGNATURE_SIZE + CRC_SIZE
            if (bytes.size != expectedTotalSize) return null

            val payload = ByteArray(payloadLength).also { buffer.get(it) }
            val signature = ByteArray(SIGNATURE_SIZE).also { buffer.get(it) }
            val receivedCrc = buffer.int

            val crc = CRC32()
            crc.update(bytes, 0, bytes.size - CRC_SIZE)
            if (crc.value.toInt() != receivedCrc) return null

            val type = PacketType.fromCode(typeCode) ?: return null

            return MeshPacket(
                version = version,
                type = type,
                flags = flags,
                ttl = ttl,
                hopCount = hopCount,
                sender = sender,
                destination = destination,
                messageId = bytesToUuid(messageIdBytes),
                timestamp = timestamp,
                payload = payload,
                signature = signature,
            )
        }

        private fun uuidToBytes(uuid: UUID): ByteArray =
            ByteBuffer.allocate(MESSAGE_ID_SIZE)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.mostSignificantBits)
                .putLong(uuid.leastSignificantBits)
                .array()

        private fun bytesToUuid(bytes: ByteArray): UUID {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
            return UUID(buffer.long, buffer.long)
        }
    }
}
