package com.mechat.app.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mechat.app.data.db.AttachmentDao
import com.mechat.app.data.db.MeChatDatabase
import java.io.File

/**
 * Deletes expired messages AND their attachment files (overwrite then delete)
 * AND any related notification. Also scheduled on app start.
 */
class ExpirationSweepWorker(
    context: Context,
    params: WorkerParameters,
    private val db: MeChatDatabase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        // Attachment files first: overwrite then delete.
        val expiredAttachments = mutableListOf<String>()
        db.messageDao().let { dao -> }
        // Query attachments of expired messages via a lightweight raw query:
        val cursor = db.openHelper.readableDatabase.query(
            "SELECT a.filePath FROM attachments a JOIN messages m ON a.messageId = m.id " +
                "WHERE m.expiresAt IS NOT NULL AND m.expiresAt < ?", arrayOf(now.toString()),
        )
        cursor.use { while (it.moveToNext()) expiredAttachments += it.getString(0) }
        for (path in expiredAttachments) {
            runCatching {
                val f = File(path)
                if (f.exists()) {
                    f.writeBytes(ByteArray(f.length().toInt()) { 0 }) // overwrite
                    f.delete()
                }
            }
        }
        db.messageDao().deleteExpired(now)
        // Cancel related notifications.
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager
        nm.cancelAll()
        return Result.success()
    }

    companion object {
        const val NAME = "mechat_expiration_sweep"

        fun scheduleOnStart(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<ExpirationSweepWorker>().build()
            androidx.work.WorkManager.getInstance(context)
                .enqueueUniqueWork(NAME, androidx.work.ExistingWorkPolicy.REPLACE, request)
        }
    }
}
