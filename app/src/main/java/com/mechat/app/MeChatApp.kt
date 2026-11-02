package com.mechat.app

import android.app.Application
import com.mechat.app.di.LockEntryPoint
import com.mechat.app.domain.lock.AppLockLifecycleObserver
import dagger.hilt.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp

/**
 * MeChat application entry point.
 *
 * GLOBAL RULE 2: no analytics, ads, crash-reporting, Firebase or any
 * tracker SDK is initialised here — and none may be added later.
 */
@HiltAndroidApp
class MeChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // App-lock: lock on background after the user-selected delay.
        val lockManager = EntryPointAccessors.fromApplication(this, LockEntryPoint::class.java)
            .appLockManager()
        AppLockLifecycleObserver.install(lockManager)
        // Disappearing messages: sweep on app start.
        com.mechat.app.data.worker.ExpirationSweepWorker.scheduleOnStart(this)
    }
}
