package com.sayanjalinexus.meshchat.di

import com.sayanjalinexus.meshchat.core.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Process-wide providers that don't belong to a specific feature module.
 *
 * Feature-specific modules (BleModule, CryptoModule, DatabaseModule,
 * RepositoryModule, NetworkModule — see ARCHITECTURE.md) are added in the
 * milestones that introduce those subsystems, keeping each module focused
 * and this one small.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(dispatcherProvider: DispatcherProvider): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatcherProvider.default)
}
