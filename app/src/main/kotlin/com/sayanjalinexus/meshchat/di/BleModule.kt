package com.sayanjalinexus.meshchat.di

import com.sayanjalinexus.meshchat.ble.AndroidBleAdvertiser
import com.sayanjalinexus.meshchat.ble.AndroidBleTransport
import com.sayanjalinexus.meshchat.ble.BleAdvertiser
import com.sayanjalinexus.meshchat.ble.BleTransport
import com.sayanjalinexus.meshchat.data.AdvertisingRepository
import com.sayanjalinexus.meshchat.data.AdvertisingRepositoryImpl
import com.sayanjalinexus.meshchat.data.PeerRepository
import com.sayanjalinexus.meshchat.data.PeerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the BLE subsystem's production implementations: scanning
 * ([BleTransport] / [PeerRepository], Milestone 3) and advertising
 * ([BleAdvertiser] / [AdvertisingRepository], Milestone 4). Test code
 * substitutes fakes ([BleTransport], [BleAdvertiser]) or mocks
 * ([com.sayanjalinexus.meshchat.ble.PermissionsManager] is concrete and
 * mockable directly) without touching this module.
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

    @Binds
    @Singleton
    abstract fun bindBleAdvertiser(impl: AndroidBleAdvertiser): BleAdvertiser

    @Binds
    @Singleton
    abstract fun bindAdvertisingRepository(impl: AdvertisingRepositoryImpl): AdvertisingRepository
}
