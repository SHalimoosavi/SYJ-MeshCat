package com.sayanjalinexus.meshchat.mesh

/**
 * Wire-format packet types, per PROTOCOL.md's "Packet Types" section.
 */
enum class PacketType(val code: Int) {
    PRIVATE_MESSAGE(0x01),
    CHANNEL_MESSAGE(0x02),
    ACK(0x03),
    PEER_ANNOUNCE(0x04),
    KEY_ROTATION(0x05),
    ;

    companion object {
        fun fromCode(code: Int): PacketType? = entries.firstOrNull { it.code == code }
    }
}
