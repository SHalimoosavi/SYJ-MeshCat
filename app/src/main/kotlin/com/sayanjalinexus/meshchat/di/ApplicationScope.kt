package com.sayanjalinexus.meshchat.di

import javax.inject.Qualifier

/**
 * Marks the [kotlinx.coroutines.CoroutineScope] that lives as long as the
 * process itself — used for work that must survive an individual screen's
 * lifecycle (e.g. the mesh router's packet relay loop, added in Milestone 5).
 * Feature-scoped work should prefer `viewModelScope` via [com.sayanjalinexus.meshchat.core.BaseViewModel]
 * instead of this scope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
