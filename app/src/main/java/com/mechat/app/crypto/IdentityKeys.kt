package com.mechat.app.crypto

import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair

/**
 * Device-local identity: an Ed25519 signing key pair and an X25519 key-agreement
 * key pair, generated with libsodium (RULE 3 — never hand-rolled crypto).
 * Private keys live only in memory here; at rest they are wrapped by
 * [KeystoreKeyProvider] and never stored unencrypted (RULE 4).
 */
class IdentityKeys(private val sodium: SodiumAndroid = SodiumAndroid()) {

    /** Ed25519 signing key pair (64-byte secret key, 32-byte public key). */
    fun generateSigningKeyPair(): KeyPair {
        val pk = ByteArray(32)
        val sk = ByteArray(64)
        sodium.crypto_sign_keypair(pk, sk)
        return KeyPair(Key.fromBytes(pk), Key.fromBytes(sk))
    }

    /** X25519 key pair for key agreement (crypto_box / X3DH-style). */
    fun generateKeyAgreementKeyPair(): KeyPair {
        val pk = ByteArray(32)
        val sk = ByteArray(32)
        sodium.crypto_box_keypair(pk, sk)
        return KeyPair(Key.fromBytes(pk), Key.fromBytes(sk))
    }

    /** Public identity ID: base64url(SHA-256(signing public key)), no padding. */
    fun publicIdFrom(signingPublicKey: ByteArray): String {
        val hash = ByteArray(32)
        sodium.crypto_hash_sha256(hash, signingPublicKey, signingPublicKey.size.toLong())
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
}
