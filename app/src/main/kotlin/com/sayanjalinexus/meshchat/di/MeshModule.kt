package com.sayanjalinexus.meshchat.di

import com.sayanjalinexus.meshchat.mesh.MeshRouter
import com.sayanjalinexus.meshchat.mesh.MeshRouterImpl
import com.sayanjalinexus.meshchat.mesh.NeighbourQualityTracker
import com.sayanjalinexus.meshchat.mesh.SeenPacketCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the mesh routing subsystem (Milestone 5). [SeenPacketCache] and
 * [NeighbourQualityTracker] are plain classes with no Android dependencies,
 * so they're provided directly rather than needing an interface + fake
 * pair the way the BLE transport/advertiser classes do — tests construct
 * them directly too (see `MeshRouterImplTest`).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MeshModule {

    @Binds
    @Singleton
    abstract fun bindMeshRouter(impl: MeshRouterImpl): MeshRouter

    companion object {
        @Provides
        @Singleton
        fun provideSeenPacketCache(): SeenPacketCache = SeenPacketCache()

        @Provides
        @Singleton
        fun provideNeighbourQualityTracker(): NeighbourQualityTracker = NeighbourQualityTracker()
    }
}
