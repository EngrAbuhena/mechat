package com.mechat.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restarts the user-controlled connection after reboot. Not exported; system-only. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ConnectionService.start(context)
        }
    }
}
