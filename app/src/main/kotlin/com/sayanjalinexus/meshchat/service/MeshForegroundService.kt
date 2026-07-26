package com.sayanjalinexus.meshchat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.sayanjalinexus.meshchat.R
import com.sayanjalinexus.meshchat.data.PeerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Keeps [PeerRepository] scanning alive while the app is backgrounded, via
 * a persistent low-priority notification (required by Android for any
 * long-running background service since API 26).
 *
 * This service does not implement BLE logic itself — it only starts/stops
 * [PeerRepository] scanning and reflects live peer count in the
 * notification. All actual scanning lives in [com.sayanjalinexus.meshchat.ble.AndroidBleTransport],
 * reached via [PeerRepository].
 */
@AndroidEntryPoint
class MeshForegroundService : LifecycleService() {

    @Inject
    lateinit var peerRepository: PeerRepository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_STOP -> {
                peerRepository.stopScanning()
                stopSelf()
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification(peerCount = 0))
                peerRepository.startScanning()
                observePeerCountForNotification()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        peerRepository.stopScanning()
        super.onDestroy()
    }

    private fun observePeerCountForNotification() {
        lifecycleScope.launch {
            peerRepository.peers.collect { peers ->
                val manager = getSystemService(NotificationManager::class.java)
                manager?.notify(NOTIFICATION_ID, buildNotification(peers.size))
            }
        }
    }

    private fun buildNotification(peerCount: Int): Notification {
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val contentText = resources.getQuantityString(
            R.plurals.mesh_service_peer_count,
            peerCount,
            peerCount,
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.mesh_service_notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification_mesh)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, getString(R.string.mesh_service_stop_action), stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.mesh_service_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.mesh_service_channel_description)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.sayanjalinexus.meshchat.action.START_SCANNING"
        const val ACTION_STOP = "com.sayanjalinexus.meshchat.action.STOP_SCANNING"

        private const val NOTIFICATION_CHANNEL_ID = "mesh_scanning"
        private const val NOTIFICATION_ID = 1001

        fun startIntent(context: Context): Intent =
            Intent(context, MeshForegroundService::class.java).setAction(ACTION_START)

        fun stopIntent(context: Context): Intent =
            Intent(context, MeshForegroundService::class.java).setAction(ACTION_STOP)
    }
}
