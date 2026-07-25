package com.sayanjalinexus.meshchat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point and root of the Hilt dependency graph.
 *
 * All app-wide singletons (crypto engine, mesh router, database, BLE
 * transport, etc., added in later milestones) are provided via Hilt
 * modules under [com.sayanjalinexus.meshchat.di] rather than initialized
 * here directly, keeping this class free of business logic.
 */
@HiltAndroidApp
class MeshChatApplication : Application()
