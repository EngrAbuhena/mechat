package com.mechat.app.crypto

import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.LibraryLoader
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.AEADBadTagException

class CryptoTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun loadSodium() {
            LibraryLoader.loadSharedLibraryIfNeeded()
        }
    }

    private val keys = IdentityKeys(SodiumJava())

    @Test
    fun `key generation produces distinct valid pairs`() {
        val signing = keys.generateSigningKeyPair()
        val kx = keys.generateKeyAgreementKeyPair()

        assertEquals(32, signing.publicKey.asBytes.size)
        assertEquals(64, signing.secretKey.asBytes.size)
        assertEquals(32, kx.publicKey.asBytes.size)
        assertEquals(32, kx.secretKey.asBytes.size)

        val other = keys.generateSigningKeyPair()
        assertNotEquals(
            signing.publicKey.asBytes.toList(),
            other.publicKey.asBytes.toList(),
        )
    }

    @Test
    fun `public id is deterministic and derived from public key only`() {
        val signing = keys.generateSigningKeyPair()
        val id1 = keys.publicIdFrom(signing.publicKey.asBytes)
        val id2 = keys.publicIdFrom(signing.publicKey.asBytes)
        assertEquals(id1, id2)
        assertTrue(id1.isNotEmpty())
    }

    @Test
    fun `wrap unwrap round trip`() {
        val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val plaintext = "secret-private-key-material".toByteArray()
        val blob = AesGcmCipher.encrypt(key, plaintext)
        assertArrayEquals(plaintext, AesGcmCipher.decrypt(key, blob))
    }

    @Test(expected = AEADBadTagException::class)
    fun `tampered ciphertext is rejected`() {
        val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val blob = AesGcmCipher.encrypt(key, "private key".toByteArray())
        blob[blob.size - 1] = (blob[blob.size - 1] + 1).toByte()
        AesGcmCipher.decrypt(key, blob)
    }

    @Test(expected = AEADBadTagException::class)
    fun `wrong key is rejected`() {
        val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val otherKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val blob = AesGcmCipher.encrypt(key, "private key".toByteArray())
        AesGcmCipher.decrypt(otherKey, blob)
    }
}
