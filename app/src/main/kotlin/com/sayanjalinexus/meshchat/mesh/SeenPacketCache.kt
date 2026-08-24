package com.sayanjalinexus.meshchat.mesh

import java.util.UUID
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Bounded, thread-safe duplicate-suppression cache keyed by message ID.
 *
 * Per PROTOCOL.md's routing rules, a node must never relay (or re-process)
 * a packet it has already seen. Entries are evicted either by LRU
 * (once [capacity] is exceeded) or by age (once older than [maxAgeMillis]),
 * whichever comes first — bounding both memory and staleness independent
 * of mesh density or how long the app has been running.
 */
class SeenPacketCache(
    private val capacity: Int = DEFAULT_CAPACITY,
    private val maxAgeMillis: Long = DEFAULT_MAX_AGE_MS,
) {
    private val lock = ReentrantLock()

    // accessOrder=true gives LRU-by-access ordering for free; removeEldestEntry
    // hooks capacity-based eviction directly into put().
    private val seen = object : LinkedHashMap<UUID, Long>(capacity, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<UUID, Long>): Boolean =
            size > capacity
    }

    /**
     * Atomically checks whether [messageId] is new and records it as seen
     * in the same operation — a single call avoids a check-then-record race
     * between concurrent callers. Returns `true` if this is the first time
     * [messageId] has been seen (or its prior entry aged out), `false` if
     * it's a duplicate of a still-fresh entry.
     */
    fun markSeenIfNew(messageId: UUID, now: Long = System.currentTimeMillis()): Boolean = lock.withLock {
        pruneExpiredLocked(now)
        val alreadySeen = seen.containsKey(messageId)
        seen[messageId] = now
        !alreadySeen
    }

    fun size(): Int = lock.withLock { seen.size }

    fun clear() = lock.withLock { seen.clear() }

    private fun pruneExpiredLocked(now: Long) {
        val cutoff = now - maxAgeMillis
        val iterator = seen.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value < cutoff) {
                iterator.remove()
            }
        }
    }

    companion object {
        const val DEFAULT_CAPACITY = 2048
        const val DEFAULT_MAX_AGE_MS = 5 * 60 * 1000L // 5 minutes
        private const val LOAD_FACTOR = 0.75f
    }
}
