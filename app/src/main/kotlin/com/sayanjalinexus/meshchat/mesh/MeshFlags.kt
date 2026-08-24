package com.sayanjalinexus.meshchat.mesh

/**
 * Named bits for the packet header's single-byte Flags field, per
 * PROTOCOL.md. Only bits 0–2 are defined so far; bits 3–7 are reserved.
 */
object MeshFlags {
    const val ENCRYPTED = 0x01
    const val FRAGMENTED = 0x02
    const val ACK_REQUESTED = 0x04
}

fun Int.hasFlag(flag: Int): Boolean = (this and flag) != 0
