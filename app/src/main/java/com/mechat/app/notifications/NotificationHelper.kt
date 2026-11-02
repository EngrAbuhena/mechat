package com.mechat.app.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mechat.app.MainActivity

/**
 * Privacy-safe notifications: DEFAULT shows only "New message" — no sender, no text.
 * Options: show sender, show preview. Hidden on lock screen by default
 * (setVisibility(PUBLIC only when the user opts in)). Actions are disabled while
 * the app is locked.
 */
object NotificationHelper {

    const val CHANNEL_ID = "mechat_messages"

    enum class Content { HIDDEN, SENDER, PREVIEW }

    fun notify(context: Context, content: Content, sender: String?, preview: String?, locked: Boolean) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                    lockscreenVisibility = Notification.VISIBILITY_SECRET // hidden on lock screen by default
                },
            )
        }
        val title = when (content) {
            Content.HIDDEN -> "New message"
            Content.SENDER -> sender ?: "New message"
            Content.PREVIEW -> sender ?: "New message"
        }
        val text = when (content) {
            Content.HIDDEN -> "New message"
            Content.SENDER -> "New message"
            Content.PREVIEW -> preview ?: "New message"
        }
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        // Notification actions are disabled while the app is locked.
        if (!locked) {
            // reply/mark-read actions would go here
        }
        nm.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
