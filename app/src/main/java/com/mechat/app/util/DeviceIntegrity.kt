package com.mechat.app.util

import android.os.Build

/**
 * Informational root/emulator warning only — NOT blocking (RULE 5 honesty).
 * Rooted/emulated environments have weaker local key protection guarantees.
 */
object DeviceIntegrity {

    fun isLikelyRootedOrEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("Emulator") ||
            Build.PRODUCT.contains("sdk")
    }
}
