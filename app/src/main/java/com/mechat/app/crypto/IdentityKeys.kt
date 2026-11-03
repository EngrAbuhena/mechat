package com.mechat.app.crypto

import com.goterl.lazysodium.Sodium
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.KeyPair

/**
 * Device-local identity: an Ed25519 signing key pair and an X25519 key-agreement
 * key pair, generated with libsodium (RULE 3 — never hand-rolled crypto).
 * Private keys live only in memory here; at rest they are wrapped by
 * [KeystoreKeyProvider] and never stored unencrypted (RULE 4).
 */
class IdentityKeys(private val sodium: SodiumAndroid = com.goterl.lazysodium.SodiumAndroid()) {

    init {
    }

    /** Ed25519 signing key pair (64-byte secret key, 32-byte public key). */
    fun generateSigningKeyPair(): KeyPair = sodium.cryptoSignKeypair()

    /** X25519 key pair for key agreement (crypto_box / X3DH-style). */
    fun generateKeyAgreementKeyPair(): KeyPair = sodium.cryptoBoxKeypair()

    /** Public identity ID: base64url(SHA-256(signing public key)), no padding. */
    fun publicIdFrom(signingPublicKey: ByteArray): String {
        val hash = sodium.cryptoHashSha256(signingPublicKey)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
}
