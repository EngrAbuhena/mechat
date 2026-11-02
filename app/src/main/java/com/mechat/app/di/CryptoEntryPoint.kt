package com.mechat.app.di

import com.mechat.app.crypto.IdentityStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Lets @Composable screens read the identity store without a ViewModel. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface CryptoEntryPoint {
    fun identityStore(): IdentityStore
}
