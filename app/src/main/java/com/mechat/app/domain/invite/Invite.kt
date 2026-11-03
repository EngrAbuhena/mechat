package com.mechat.app.domain.invite

import com.goterl.lazysodium.Sodium
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import java.security.SecureRandom
import java.util.Base64

/**
 * One-time invite: our public signing key + X25519 public key + random invite token +
 * signalling address, signed with our Ed25519 key. Encoded as copyable text
 * ("MCHAT1.<base64url payload>.<base64url signature>"). No directory, no phone numbers.
 */
object InviteCodec {

    const val PREFIX = "MCHAT1"
    const val DEFAULT_TTL_MS = 24 * 60 * 60 * 1000L // 24h

    data class Invite(
        val signingPublicKey: ByteArray,
        val keyAgreementPublicKey: ByteArray,
        val token: ByteArray,
        val relayAddress: String,
        val expiresAt: Long,
    )

    private fun b64(bytes: ByteArray) = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    private fun unb64(s: String) = Base64.getUrlDecoder().decode(s)

    fun create(
        sodium: com.goterl.lazysodium.LazySodium = com.goterl.lazysodium.LazySodiumAndroid(SodiumAndroid()),
        signingKeyPair: com.goterl.lazysodium.utils.KeyPair,
        keyAgreementPublicKey: ByteArray,
        relayAddress: String,
        ttlMs: Long = DEFAULT_TTL_MS,
        now: Long = System.currentTimeMillis(),
    ): Pair<Invite, String> {
        val token = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val invite = Invite(
            signingPublicKey = signingKeyPair.publicKey.asBytes,
            keyAgreementPublicKey = keyAgreementPublicKey,
            token = token,
            relayAddress = relayAddress,
            expiresAt = now + ttlMs,
        )
        val payload = encodePayload(invite)
        val sig = sodium.cryptoSignDetached(payload, signingKeyPair.secretKey.asBytes)
        return invite to "$PREFIX.$payload.${b64(sig)}"
    }

    private fun encodePayload(i: Invite): String {
        // payload = spk(32) || kx(32) || token(16) || expiresAt(8) || relay
        val relay = i.relayAddress.toByteArray()
        val buf = ByteArray(32 + 32 + 16 + 8 + relay.size)
        i.signingPublicKey.copyInto(buf, 0)
        i.keyAgreementPublicKey.copyInto(buf, 32)
        i.token.copyInto(buf, 64)
        java.nio.ByteBuffer.wrap(buf, 80, 8).putLong(i.expiresAt)
        relay.copyInto(buf, 88)
        return b64(buf)
    }

    /** Decode + verify signature. Returns null if malformed or signature invalid. */
    fun parse(sodium: Sodium, text: String, trustedSigningKey: ByteArray? = null): Invite? {
        val parts = text.trim().split(".")
        if (parts.size != 3 || parts[0] != PREFIX) return null
        val payload = try { unb64(parts[1]) } catch (e: Exception) { return null }
        if (payload.size < 88) return null
        val sig = try { unb64(parts[2]) } catch (e: Exception) { return null }
        val spk = payload.copyOfRange(0, 32)
        if (!sodium.cryptoSignVerifyDetached(sig, payload, spk)) return null
        if (trustedSigningKey != null && !spk.contentEquals(trustedSigningKey)) return null
        val relay = String(payload, 88)
        return Invite(
            signingPublicKey = spk,
            keyAgreementPublicKey = payload.copyOfRange(32, 64),
            token = payload.copyOfRange(64, 80),
            relayAddress = relay,
            expiresAt = java.nio.ByteBuffer.wrap(payload, 80, 8).long,
        )
    }

    fun isExpired(invite: Invite, now: Long = System.currentTimeMillis()) =
        now >= invite.expiresAt
}
