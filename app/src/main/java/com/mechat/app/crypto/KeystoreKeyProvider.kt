package com.mechat.app.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyFactory
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM key that never leaves the Android Keystore
 * (hardware-backed / StrongBox when available, graceful fallback otherwise).
 * Used to wrap the private identity keys at rest. RULE 3/4.
 */
class KeystoreKeyProvider(
    private val alias: String = "mechat_identity_wrap_key",
) {
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    /** Returns the existing key or generates a new one (StrongBox preferred on API 28+). */
    fun getOrCreateKey(): SecretKey {
        (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return generateKey()
    }

    private fun generateKey(): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)

        // Prefer StrongBox (discrete secure element) when available; fall back gracefully.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                builder.setIsStrongBoxBacked(true)
                return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                    .apply { init(builder.build()) }
                    .generateKey()
            } catch (e: Exception) {
                // No StrongBox on this device — fall through to TEE-backed key.
            }
        }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply { init(builder.build()) }
            .generateKey()
    }

    /** Encrypt with the Keystore key. IV is prepended; tag appended by GCM. */
    fun wrap(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        return iv + cipher.doFinal(plaintext)
    }

    /** Decrypt; throws AEADBadTagException if the ciphertext was tampered with. */
    fun unwrap(blob: ByteArray): ByteArray {
        val iv = blob.copyOfRange(0, 12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(blob, 12, blob.size - 12)
    }

    /** Reports whether the wrap key is hardware-backed (KeyInfo). */
    fun keyInfo(): KeyInfo {
        val key = getOrCreateKey()
        val factory = KeyFactory.getInstance(key.algorithm, "AndroidKeyStore")
        return factory.getKeySpec(key, KeyInfo::class.java)
    }

    val isHardwareBacked: Boolean get() = keyInfo().isInsideSecureHardware
}
