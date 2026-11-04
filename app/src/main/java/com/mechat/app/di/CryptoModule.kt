package com.mechat.app.di

import com.goterl.lazysodium.Sodium
import com.goterl.lazysodium.SodiumAndroid
import com.mechat.app.crypto.IdentityKeys
import com.mechat.app.crypto.KeystoreKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides
    @Singleton
    fun provideSodium(): Sodium = SodiumAndroid()

    @Provides
    @Singleton
    fun provideIdentityKeys(sodium: Sodium): IdentityKeys = IdentityKeys(sodium)

    @Provides
    @Singleton
    fun provideKeystoreKeyProvider(): KeystoreKeyProvider = KeystoreKeyProvider()
}
