package com.mechat.app.crypto

import com.goterl.lazysodium.Sodium
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Pwhash
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * PIN hashing with Argon2id via libsodium crypto_pwhash (RULE 3 — NEVER plain SHA256).
 * Random per-install salt; only hash + salt + parameters are stored.
 * Verification uses a constant-time comparison.
 */
class PinHasher(private val sodium: com.goterl.lazysodium.LazySodium = com.goterl.lazysodium.LazySodium(SodiumAndroid())) {

    companion object {
        const val HASH_BYTES = 32
        const val SALT_BYTES = 16
        // Sensible phone costs: 64 MiB memory, 3 iterations (Argon2id).
        const val OPS_LIMIT = 3L
        const val MEM_LIMIT = 64L * 1024L * 1024L
        const val MIN_PIN = 6
        const val MAX_PIN = 12
    }

    data class PinHash(
        val hash: ByteArray,
        val salt: ByteArray,
        val opsLimit: Long = OPS_LIMIT,
        val memLimit: Long = MEM_LIMIT,
        val algorithm: Int = Pwhash.ALG_ARGON2ID13,
    )

    fun isValidPinLength(pin: String): Boolean =
        pin.length in MIN_PIN..MAX_PIN && pin.all { it.isDigit() }

    fun hash(pin: String): PinHash {
        require(isValidPinLength(pin))
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = sodium.cryptoPwhash(
            HASH_BYTES, pin.toByteArray(), salt, OPS_LIMIT, MEM_LIMIT, Pwhash.ALG_ARGON2ID13,
        )
        return PinHash(hash, salt)
    }

    /** Constant-time verification. */
    fun verify(pin: String, stored: PinHash): Boolean {
        if (!isValidPinLength(pin)) return false
        val candidate = sodium.cryptoPwhash(
            stored.hash.size, pin.toByteArray(), stored.salt,
            stored.opsLimit, stored.memLimit, stored.algorithm,
        )
        return MessageDigest.isEqual(candidate, stored.hash)
    }
}
