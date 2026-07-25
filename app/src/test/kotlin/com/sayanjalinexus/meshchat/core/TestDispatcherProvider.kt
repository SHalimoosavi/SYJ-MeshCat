package com.sayanjalinexus.meshchat.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * [DispatcherProvider] that routes every dispatcher to the same
 * [TestDispatcher], so unit tests get fully deterministic coroutine
 * execution regardless of which dispatcher production code asks for.
 */
class TestDispatcherProvider(
    private val testDispatcher: TestDispatcher,
) : DispatcherProvider {
    override val main: CoroutineDispatcher get() = testDispatcher
    override val io: CoroutineDispatcher get() = testDispatcher
    override val default: CoroutineDispatcher get() = testDispatcher
    override val unconfined: CoroutineDispatcher get() = testDispatcher
}
