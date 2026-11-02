package com.mechat.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * User-controlled foreground service that keeps ONE WebSocket open so messages
 * arrive while the app is closed — no Firebase/Google push (RULE 2).
 * The persistent low-priority notification clearly explains why it is shown.
 * WorkManager periodic mailbox checks run as a fallback (handles Doze).
 */
class ConnectionService : Service() {

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            "mechat_connection",
            "Connection",
            NotificationManager.IMPORTANCE_LOW, // low priority, no sound
        ).apply {
            description = "Keeps one encrypted connection open so you receive messages while MeChat is closed. No content is shown."
        }
        nm.createNotificationChannel(channel)
        val notification = Notification.Builder(this, "mechat_connection")
            .setContentTitle("MeChat is connected")
            .setContentText("Receiving messages in the background. Tap Settings to change this.")
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(2, notification)
        }
        scheduleMailboxFallback()
    }

    private fun scheduleMailboxFallback() {
        val request = PeriodicWorkRequestBuilder<MailboxFallbackWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "mechat_mailbox_fallback", ExistingPeriodicWorkPolicy.KEEP, request,
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val i = Intent(context, ConnectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
            else context.startService(i)
        }
    }
}
