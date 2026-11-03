package com.mechat.app.domain.invite

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.goterl.lazysodium.LazySodiumAndroid\nimport com.goterl.lazysodium.SodiumAndroid\nimport com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

private val Context.invitePrefs by preferencesDataStore(name = "mechat_invites")

/**
 * Issues one-time invites and rejects expired or already-used tokens.
 * Used-token IDs are persisted so they survive restarts (one-time = once, ever).
 */
@Singleton
class InviteManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val sodium = LazySodiumAndroid(SodiumAndroid())
    private val usedKey = stringPreferencesKey("used_tokens_v1")

    fun createInvite(
        signingKeyPair: com.goterl.lazysodium.utils.KeyPair,
        keyAgreementPublicKey: ByteArray,
        relayAddress: String,
        ttlMs: Long = InviteCodec.DEFAULT_TTL_MS,
    ): String {
        val (_, text) = InviteCodec.create(
            sodium, signingKeyPair, keyAgreementPublicKey, relayAddress, ttlMs,
        )
        return text
    }

    /**
     * Validates an incoming invite text. Returns the invite only if the format and
     * signature are valid, it has not expired, and its token was never used.
     * On success the token is marked used (one-time).
     */
    fun acceptInvite(text: String): InviteCodec.Invite? = runBlocking {
        val invite = InviteCodec.parse(sodium, text) ?: return@runBlocking null
        if (InviteCodec.isExpired(invite)) return@runBlocking null
        val tokenId = Base64.encodeToString(invite.token, Base64.NO_WRAP)
        val prefs = context.invitePrefs.data.first()
        val used = prefs[usedKey]?.split(",")?.toSet() ?: emptySet()
        if (tokenId in used) return@runBlocking null // reuse rejected
        context.invitePrefs.edit {
            it[usedKey] = (used + tokenId).joinToString(",")
        }
        invite
    }
}
