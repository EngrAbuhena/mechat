package com.mechat.app.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM AEAD, format: [12-byte IV][ciphertext+tag].
 * Pure function over a raw key — unit-testable on the JVM.
 * In production the key never leaves the Android Keystore (see KeystoreKeyProvider).
 */
object AesGcmCipher {
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private val random = SecureRandom()

    fun encrypt(key: ByteArray, plaintext: ByteArray): ByteArray {
        require(key.size == 32) { "AES-256 requires a 32-byte key" }
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, javax.crypto.spec.SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        return iv + cipher.doFinal(plaintext)
    }

    fun decrypt(key: ByteArray, blob: ByteArray): ByteArray {
        require(key.size == 32) { "AES-256 requires a 32-byte key" }
        require(blob.size > IV_BYTES + TAG_BITS / 8) { "Ciphertext too short" }
        val iv = blob.copyOfRange(0, IV_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, javax.crypto.spec.SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(blob, IV_BYTES, blob.size - IV_BYTES) // throws AEADBadTagException on tampering
    }
}
