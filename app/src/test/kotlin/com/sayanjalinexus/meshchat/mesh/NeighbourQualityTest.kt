package com.sayanjalinexus.meshchat.mesh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeighbourQualityTest {

    @Test
    fun `normalizeRssi maps the floor to 0 and the ceiling to 1`() {
        assertEquals(0.0, NeighbourQuality.normalizeRssi(-100), 0.0001)
        assertEquals(1.0, NeighbourQuality.normalizeRssi(-30), 0.0001)
    }

    @Test
    fun `normalizeRssi clamps values outside the expected range`() {
        assertEquals(0.0, NeighbourQuality.normalizeRssi(-120), 0.0001)
        assertEquals(1.0, NeighbourQuality.normalizeRssi(-10), 0.0001)
    }

    @Test
    fun `normalizeRssi is monotonic within range`() {
        val weak = NeighbourQuality.normalizeRssi(-90)
        val medium = NeighbourQuality.normalizeRssi(-60)
        val strong = NeighbourQuality.normalizeRssi(-40)

        assertTrue(weak < medium)
        assertTrue(medium < strong)
    }

    @Test
    fun `overallScore is the midpoint for a freshly created neighbour`() {
        val quality = NeighbourQuality(address = "AA:BB:CC:DD:EE:FF")

        assertEquals(0.5, quality.overallScore, 0.0001)
    }

    @Test
    fun `tracker recordRssi updates only the rssi component`() {
        val tracker = NeighbourQualityTracker()

        tracker.recordRssi("AA:BB:CC:DD:EE:FF", rssiDbm = -30)

        val score = tracker.scoreFor("AA:BB:CC:DD:EE:FF")
        assertEquals(1.0, score.rssiScore, 0.0001)
        assertEquals(0.5, score.stabilityScore, 0.0001)
    }

    @Test
    fun `tracker recordReconnect decreases stability as reconnects accumulate`() {
        val tracker = NeighbourQualityTracker()
        val address = "AA:BB:CC:DD:EE:FF"

        tracker.recordReconnect(address)
        val afterOne = tracker.scoreFor(address).stabilityScore

        tracker.recordReconnect(address)
        val afterTwo = tracker.scoreFor(address).stabilityScore

        assertTrue(afterTwo < afterOne)
    }

    @Test
    fun `tracker recordRelayOutcome computes a running success ratio`() {
        val tracker = NeighbourQualityTracker()
        val address = "AA:BB:CC:DD:EE:FF"

        tracker.recordRelayOutcome(address, success = true)
        tracker.recordRelayOutcome(address, success = true)
        tracker.recordRelayOutcome(address, success = false)

        assertEquals(2.0 / 3.0, tracker.scoreFor(address).relaySuccessScore, 0.0001)
    }

    @Test
    fun `allScores is sorted by overall score descending`() {
        val tracker = NeighbourQualityTracker()
        tracker.recordRssi("weak", rssiDbm = -95)
        tracker.recordRssi("strong", rssiDbm = -35)

        val scores = tracker.allScores()

        assertEquals("strong", scores.first().address)
        assertEquals("weak", scores.last().address)
    }

    @Test
    fun `remove clears all tracked signals for an address`() {
        val tracker = NeighbourQualityTracker()
        val address = "AA:BB:CC:DD:EE:FF"
        tracker.recordRssi(address, rssiDbm = -30)
        tracker.recordReconnect(address)

        tracker.remove(address)

        // Removed entries reset to the default midpoint score.
        assertEquals(0.5, tracker.scoreFor(address).overallScore, 0.0001)
    }
}
