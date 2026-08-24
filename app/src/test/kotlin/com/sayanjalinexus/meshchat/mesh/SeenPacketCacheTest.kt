package com.sayanjalinexus.meshchat.mesh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SeenPacketCacheTest {

    @Test
    fun `first sighting of a message id returns true`() {
        val cache = SeenPacketCache()

        assertTrue(cache.markSeenIfNew(UUID.randomUUID()))
    }

    @Test
    fun `second sighting of the same message id returns false`() {
        val cache = SeenPacketCache()
        val id = UUID.randomUUID()

        cache.markSeenIfNew(id)
        val secondResult = cache.markSeenIfNew(id)

        assertFalse(secondResult)
    }

    @Test
    fun `different message ids are independently tracked`() {
        val cache = SeenPacketCache()

        assertTrue(cache.markSeenIfNew(UUID.randomUUID()))
        assertTrue(cache.markSeenIfNew(UUID.randomUUID()))
        assertEquals(2, cache.size())
    }

    @Test
    fun `capacity eviction drops the least recently used entry`() {
        val cache = SeenPacketCache(capacity = 2)
        val first = UUID.randomUUID()
        val second = UUID.randomUUID()
        val third = UUID.randomUUID()

        cache.markSeenIfNew(first, now = 1000L)
        cache.markSeenIfNew(second, now = 2000L)
        // Third insertion exceeds capacity=2, evicting the least recently
        // used entry, which is `first` (never re-accessed since insertion).
        cache.markSeenIfNew(third, now = 3000L)

        assertEquals(2, cache.size())
        // `first` was evicted, so it's treated as new again.
        assertTrue(cache.markSeenIfNew(first, now = 4000L))
    }

    @Test
    fun `age-based expiry treats a stale entry as new again`() {
        val cache = SeenPacketCache(maxAgeMillis = 1000L)
        val id = UUID.randomUUID()

        cache.markSeenIfNew(id, now = 0L)
        // Still within the age window.
        assertFalse(cache.markSeenIfNew(id, now = 500L))
        // Past the age window — the stale entry is pruned and this reads as new.
        assertTrue(cache.markSeenIfNew(id, now = 2000L))
    }

    @Test
    fun `clear empties the cache`() {
        val cache = SeenPacketCache()
        cache.markSeenIfNew(UUID.randomUUID())

        cache.clear()

        assertEquals(0, cache.size())
    }
}
