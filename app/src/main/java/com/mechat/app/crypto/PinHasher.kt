package com.mechat.app.crypto

import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.PwHash
import com.sun.jna.NativeLong
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * PIN hashing with Argon2id via libsodium crypto_pwhash (RULE 3 — NEVER plain SHA256).
 * Random per-install salt; only hash + salt + parameters are stored.
 * Verification uses a constant-time comparison.
 */
class PinHasher(private val sodium: SodiumAndroid = SodiumAndroid()) {

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
        val algorithm: Int = PwHash.ALG_ARGON2ID13,
    )

    fun isValidPinLength(pin: String): Boolean =
        pin.length in MIN_PIN..MAX_PIN && pin.all { it.isDigit() }

    fun hash(pin: String): PinHash {
        require(isValidPinLength(pin))
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = ByteArray(HASH_BYTES)
        sodium.crypto_pwhash(
            hash, HASH_BYTES.toLong(), pin.toByteArray(), pin.length.toLong(), salt,
            OPS_LIMIT, NativeLong(MEM_LIMIT), PwHash.ALG_ARGON2ID13,
        )
        return PinHash(hash, salt)
    }

    /** Constant-time verification. */
    fun verify(pin: String, stored: PinHash): Boolean {
        if (!isValidPinLength(pin)) return false
        val candidate = ByteArray(stored.hash.size)
        sodium.crypto_pwhash(
            candidate, stored.hash.size.toLong(), pin.toByteArray(), pin.length.toLong(),
            stored.salt, stored.opsLimit, NativeLong(stored.memLimit), stored.algorithm,
        )
        return MessageDigest.isEqual(candidate, stored.hash)
    }
}
