package com.sayanjalinexus.meshchat.di

import com.sayanjalinexus.meshchat.ble.AndroidBleTransport
import com.sayanjalinexus.meshchat.ble.BleTransport
import com.sayanjalinexus.meshchat.data.PeerRepository
import com.sayanjalinexus.meshchat.data.PeerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the BLE scanning subsystem's production implementations. Test code
 * substitutes fakes ([BleTransport]) or mocks ([PermissionsManager] is
 * concrete and mockable directly) without touching this module.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BleModule {

    @Binds
    @Singleton
    abstract fun bindBleTransport(impl: AndroidBleTransport): BleTransport

    @Binds
    @Singleton
    abstract fun bindPeerRepository(impl: PeerRepositoryImpl): PeerRepository
}
