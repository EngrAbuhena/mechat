package com.mechat.app.domain.lock

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Locks the app when it goes to the background, after the user-selected delay
 * (immediately / 30s / 5min). While locked, MainActivity sets FLAG_SECURE so the
 * Recents thumbnail is blank.
 */
object AppLockLifecycleObserver : DefaultLifecycleObserver {

    private var stopTime = 0L

    fun install(appLockManager: AppLockManager) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                stopTime = System.currentTimeMillis()
                val delay = appLockManager.lockDelay.ms
                if (delay == 0L) {
                    appLockManager.lock()
                } else {
                    // Simple approach: schedule via the delay on next start check.
                    pendingLock(appLockManager, delay)
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                if (stopTime != 0L && System.currentTimeMillis() - stopTime >= appLockManager.lockDelay.ms) {
                    appLockManager.lock()
                }
                stopTime = 0L
            }
        })
    }

    private fun pendingLock(manager: AppLockManager, delayMs: Long) {
        Thread {
            Thread.sleep(delayMs)
            manager.lock()
        }.apply { isDaemon = true }.start()
    }
}
