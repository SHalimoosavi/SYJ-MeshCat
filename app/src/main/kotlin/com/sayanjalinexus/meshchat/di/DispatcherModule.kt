package com.sayanjalinexus.meshchat.di

import com.sayanjalinexus.meshchat.core.DefaultDispatcherProvider
import com.sayanjalinexus.meshchat.core.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the production [DispatcherProvider] implementation app-wide.
 * Instrumentation/unit tests can install a test-only module that binds a
 * fake instead, without touching production code.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}
