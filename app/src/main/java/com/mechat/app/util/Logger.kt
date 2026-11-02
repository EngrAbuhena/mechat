package com.mechat.app.util

import android.util.Log
import com.mechat.app.BuildConfig

/**
 * No-op in release (RULE 4): never log message content, keys, PINs or IDs.
 * Debug builds may log non-sensitive diagnostics only.
 */
object Logger {
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.d(tag, msg)
    }

    fun e(tag: String, msg: String, t: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, msg, t)
    }
}
