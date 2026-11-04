package com.mechat.app.crypto

import com.goterl.lazysodium.Sodium
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair

/**
 * * Device-local identity: Ed25519 signing key pair + X25519 key-agreement pair,
 * generated with libsodium (RULE 3 — never hand-rolled crypto).
 */
class IdentityKeys(private val sodium: Sodium = SodiumAndroid()) {

    fun generateSigningKeyPair(): KeyPair {
        val pk = ByteArray(32)
        val sk = ByteArray(64)
        sodium.crypto_sign_keypair(pk, sk)
        return KeyPair(Key.fromBytes(pk), Key.fromBytes(sk))
    }

    fun generateKeyAgreementKeyPair(): KeyPair {
        val pk = ByteArray(32)
        val sk = ByteArray(32)
        sodium.crypto_box_keypair(pk, sk)
        return KeyPair(Key.fromBytes(pk), Key.fromBytes(sk))
    }

    fun publicIdFrom(signingPublicKey: ByteArray): String {
        val hash = ByteArray(32)
        sodium.crypto_hash_sha256(hash, signingPublicKey, signingPublicKey.size.toLong())
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
}
