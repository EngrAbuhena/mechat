package com.mechat.app.crypto

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "mechat_identity")

/**
 * Persists the wrapped identity. The blob is AES-256-GCM ciphertext produced by the
 * Android Keystore key — private key bytes are never written unencrypted (RULE 4).
 * No device identifiers are used or stored anywhere.
 */
@Singleton
class IdentityStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystore: KeystoreKeyProvider,
    private val identityKeys: IdentityKeys,
) {
    private val wrappedKey = stringPreferencesKey("wrapped_identity_v1")

    /** Generates the identity on first launch, or returns the existing public ID. */
    fun ensureIdentity(): String = runBlocking {
        val prefs = context.dataStore.data.first()
        prefs[wrappedKey]?.let { blob ->
            val raw = keystore.unwrap(Base64.decode(blob, Base64.DEFAULT))
            // raw = signingSecret(64) || signingPublic(32) || x25519Secret(32) || x25519Public(32)
            return@runBlocking identityKeys.publicIdFrom(raw.copyOfRange(64, 96))
        }
        val signing = identityKeys.generateSigningKeyPair()
        val kx = identityKeys.generateKeyAgreementKeyPair()
        val raw = signing.secretKey.asBytes + signing.publicKey.asBytes +
            kx.secretKey.asBytes + kx.publicKey.asBytes
        val wrapped = Base64.encodeToString(keystore.wrap(raw), Base64.NO_WRAP)
        context.dataStore.edit { it[wrappedKey] = wrapped }
        identityKeys.publicIdFrom(signing.publicKey.asBytes)
    }

    /** KeyInfo for the debug section of Settings. */
    fun keyInfo() = keystore.keyInfo()
}
