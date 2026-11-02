package com.mechat.app.di

import com.mechat.app.data.wipe.Wiper
import com.mechat.app.domain.lock.AppLockManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LockEntryPoint {
    fun appLockManager(): AppLockManager
    fun wiper(): Wiper
}
