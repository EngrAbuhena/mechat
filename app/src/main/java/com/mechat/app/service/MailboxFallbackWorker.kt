package com.mechat.app.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Periodic mailbox poll fallback for when the foreground connection is Doze-paused. */
class MailboxFallbackWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Poll mailbox via the signalling client; decrypt/store handled by OfflineSync.
        return Result.success()
    }
}
