package com.sayanjalinexus.meshchat.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Indirection over [Dispatchers] so every layer (repositories, use cases,
 * BLE transport, routing) asks for a dispatcher through injection instead
 * of referencing [Dispatchers] directly. Unit tests substitute a fake
 * implementation backed by a single [kotlinx.coroutines.test.TestDispatcher],
 * which is what makes coroutine-heavy code (routing, crypto, DB access)
 * deterministically testable.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
}
