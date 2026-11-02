package com.mechat.app.data.wipe

import android.content.Context
import com.mechat.app.data.db.MeChatDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Panic wipe: deletes the encrypted database, all key material (Keystore aliases,
 * wrapped blobs in preferences), attachments, cache and preferences, resetting the
 * app to first-launch state. RULE 4: no key bytes are ever logged.
 * Honest limits are documented in docs/threat-model.md.
 */
@Singleton
class Wiper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: MeChatDatabase,
) {
    private val keyAliases = listOf("mechat_identity_wrap_key", "mechat_db_wrap_key")

    fun wipeAll() {
        runCatching { db.close() }
        // Encrypted database + journals.
        context.deleteDatabase("mechat.db")
        // Android Keystore aliases holding the wrap keys.
        runCatching {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyAliases.forEach { runCatching { ks.deleteEntry(it) } }
        }
        // Preferences (wrapped keys, PIN hashes, lockout counters).
        File(context.filesDir.parentFile, "shared_prefs").listFiles()?.forEach { it.delete() }
        File(context.filesDir.parentFile, "datastore").listFiles()?.forEach { it.delete() }
        // Encrypted local attachments.
        File(context.filesDir, "attachments").deleteRecursively()
        // Cache.
        context.cacheDir.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
    }
}
