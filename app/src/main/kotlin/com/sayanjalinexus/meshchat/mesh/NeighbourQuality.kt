package com.sayanjalinexus.meshchat.mesh

/**
 * A per-neighbour quality snapshot, combining RSSI, connection stability,
 * and relay/ack success ratio into a single [overallScore] — used to
 * prefer higher-quality neighbours when multiple relay paths exist, and to
 * deprioritize flaky connections for battery efficiency (per
 * ARCHITECTURE.md's "Neighbour Quality Score" section).
 *
 * This is intentionally a simple, tunable weighted average rather than
 * anything adaptive — the mesh is small-scale and the goal is "good enough
 * to break ties between candidate relay paths," not precision.
 */
data class NeighbourQuality(
    val address: String,
    /** Normalized 0..1 from RSSI; higher = stronger signal. */
    val rssiScore: Double = MIDPOINT_SCORE,
    /** Normalized 0..1; higher = fewer reconnects observed. */
    val stabilityScore: Double = MIDPOINT_SCORE,
    /** Normalized 0..1; higher = more successful relays/acks. */
    val relaySuccessScore: Double = MIDPOINT_SCORE,
) {
    val overallScore: Double
        get() = (rssiScore * RSSI_WEIGHT) +
            (stabilityScore * STABILITY_WEIGHT) +
            (relaySuccessScore * RELAY_WEIGHT)

    companion object {
        private const val MIDPOINT_SCORE = 0.5
        private const val RSSI_WEIGHT = 0.4
        private const val STABILITY_WEIGHT = 0.3
        private const val RELAY_WEIGHT = 0.3

        private const val RSSI_FLOOR = -100
        private const val RSSI_CEILING = -30

        /** Maps a typical BLE RSSI range ([RSSI_FLOOR]..[RSSI_CEILING] dBm) to a 0..1 score. */
        fun normalizeRssi(rssiDbm: Int): Double {
            val clamped = rssiDbm.coerceIn(RSSI_FLOOR, RSSI_CEILING)
            return (clamped - RSSI_FLOOR).toDouble() / (RSSI_CEILING - RSSI_FLOOR)
        }
    }
}

/**
 * Mutable, thread-safe tracker producing an updated [NeighbourQuality] per
 * peer address as new signals (RSSI samples, reconnect events, relay
 * outcomes) arrive over time.
 */
class NeighbourQualityTracker {
    private val lock = Any()
    private val scores = mutableMapOf<String, NeighbourQuality>()
    private val reconnectCounts = mutableMapOf<String, Int>()
    private val relayOutcomes = mutableMapOf<String, Pair<Int, Int>>() // successCount to totalCount

    fun recordRssi(address: String, rssiDbm: Int): Unit = synchronized(lock) {
        val current = scores[address] ?: NeighbourQuality(address)
        scores[address] = current.copy(rssiScore = NeighbourQuality.normalizeRssi(rssiDbm))
    }

    /** Call each time a previously-connected neighbour reconnects (a stability signal). */
    fun recordReconnect(address: String): Unit = synchronized(lock) {
        val count = (reconnectCounts[address] ?: 0) + 1
        reconnectCounts[address] = count
        val stability = 1.0 / (1 + count)
        val current = scores[address] ?: NeighbourQuality(address)
        scores[address] = current.copy(stabilityScore = stability)
    }

    fun recordRelayOutcome(address: String, success: Boolean): Unit = synchronized(lock) {
        val (successCount, total) = relayOutcomes[address] ?: (0 to 0)
        val newSuccess = if (success) successCount + 1 else successCount
        val newTotal = total + 1
        relayOutcomes[address] = newSuccess to newTotal
        val ratio = newSuccess.toDouble() / newTotal
        val current = scores[address] ?: NeighbourQuality(address)
        scores[address] = current.copy(relaySuccessScore = ratio)
    }

    fun scoreFor(address: String): NeighbourQuality = synchronized(lock) {
        scores[address] ?: NeighbourQuality(address)
    }

    fun allScores(): List<NeighbourQuality> = synchronized(lock) {
        scores.values.sortedByDescending { it.overallScore }
    }

    fun remove(address: String): Unit = synchronized(lock) {
        scores.remove(address)
        reconnectCounts.remove(address)
        relayOutcomes.remove(address)
    }
}
