package com.mechat.app.data.db

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mechat.app.crypto.KeystoreKeyProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

private val Context.keyDataStore by preferencesDataStore(name = "mechat_dbkey")

/**
 * Database key: a random 32-byte key generated ONCE, before the database is ever
 * created, and stored only wrapped by an Android Keystore AES-256-GCM key.
 * SQLCipher receives the raw key bytes (PRAGMA key = "x'<hex>'") — no passphrase KDF.
 * The database must never exist unencrypted, not even on first run.
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystore: KeystoreKeyProvider, // separate alias below
) {
    private val wrappedKey = stringPreferencesKey("wrapped_db_key_v1")
    private val keystoreAlias = "mechat_db_wrap_key"

    private val keystoreProvider by lazy { KeystoreKeyProvider(keystoreAlias) }

    /** Returns the raw 32-byte database key, generating + wrapping it first if needed. */
    fun getOrCreateRawKey(): ByteArray = runBlocking {
        val prefs = context.keyDataStore.data.first()
        prefs[wrappedKey]?.let { blob ->
            return@runBlocking keystoreProvider.unwrap(Base64.decode(blob, Base64.DEFAULT))
        }
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val wrapped = Base64.encodeToString(keystoreProvider.wrap(raw), Base64.NO_WRAP)
        context.keyDataStore.edit { it[wrappedKey] = wrapped }
        raw
    }
}
