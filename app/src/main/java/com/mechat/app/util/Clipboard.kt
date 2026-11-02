package com.mechat.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/** Clipboard helpers: flag sensitive content; auto-clear invite codes after 60s. */
object SensitiveClipboard {

    fun copySensitive(context: Context, label: String, text: String, autoClearSeconds: Long = 60L) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        // Flag as sensitive so it is not shown in clipboard previews/overlay.
        clip.description.extras.putBoolean("android.content.extra.IS_SENSITIVE", true)
        cm.setPrimaryClip(clip)
        // Auto-clear after the timeout (invite codes).
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val current = cm.primaryClip
            if (current != null && current.itemCount > 0 &&
                current.getItemAt(0).text == text
            ) {
                cm.setPrimaryClip(ClipData.newPlainText(label, ""))
            }
        }, autoClearSeconds * 1000L)
    }
}
