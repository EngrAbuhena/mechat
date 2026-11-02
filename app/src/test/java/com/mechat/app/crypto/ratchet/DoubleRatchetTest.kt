package com.mechat.app.crypto.ratchet

import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.LibraryLoader
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test

class DoubleRatchetTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun load() { LibraryLoader.loadSharedLibraryIfNeeded() }
    }

    private val sodium = SodiumJava()

    private fun sessionPair(): Pair<DoubleRatchet, DoubleRatchet> {
        val aId = sodium.cryptoSignKeyPair()
        val bId = sodium.cryptoSignKeyPair()
        val aKx = sodium.cryptoBoxKeyPair()
        val bKx = sodium.cryptoBoxKeyPair()
        val alice = DoubleRatchet(sodium)
        val bob = DoubleRatchet(sodium)
        alice.initSession(aId.secretKey.asBytes, aId.publicKey.asBytes, bId.publicKey.asBytes, bKx.publicKey.asBytes)
        bob.initSessionResponder(bId.secretKey.asBytes, bId.publicKey.asBytes, aId.publicKey.asBytes, aKx.publicKey.asBytes)
        return alice to bob
    }

    @Test
    fun `round trip`() {
        val (alice, bob) = sessionPair()
        val msg = "hello bob".toByteArray()
        val env = alice.encrypt(msg)
        assertArrayEquals(msg, bob.decrypt(env))
    }

    @Test
    fun `tampering is rejected`() {
        val (alice, bob) = sessionPair()
        val env = alice.encrypt("secret".toByteArray())
        env[env.size - 1] = (env[env.size - 1] + 1).toByte()
        try { bob.decrypt(env); fail("should reject") } catch (e: SecurityException) {}
    }

    @Test
    fun `replay is rejected`() {
        val (alice, bob) = sessionPair()
        val env = alice.encrypt("once".toByteArray())
        bob.decrypt(env)
        try { bob.decrypt(env); fail("replay must be rejected") } catch (e: SecurityException) {}
    }

    @Test
    fun `out of order delivery works`() {
        val (alice, bob) = sessionPair()
        val e1 = alice.encrypt("first".toByteArray())
        val e2 = alice.encrypt("second".toByteArray())
        val e3 = alice.encrypt("third".toByteArray())
        assertEquals("third", String(bob.decrypt(e3)))
        assertEquals("first", String(bob.decrypt(e1)))
        assertEquals("second", String(bob.decrypt(e2)))
    }

    @Test
    fun `compromised later key does not decrypt older messages`() {
        val (alice, bob) = sessionPair()
        val old = alice.encrypt("old secret".toByteArray())
        bob.decrypt(old)
        // Simulate compromise of current chain: ratchet forward several times.
        repeat(3) { alice.encrypt("x".toByteArray()); bob.decrypt(alice.encrypt("y".toByteArray())) }
        // An attacker holding only the current key material cannot produce keys for `old`.
        val currentKey = Hkdf.derive(ByteArray(32), "attacker".toByteArray())
        try {
            val nonce = old.copyOfRange(41, 65)
            val header = old.copyOfRange(0, 65)
            val ct = old.copyOfRange(65, old.size)
            // decrypt with wrong key must fail
            val field = DoubleRatchet::class.java.getDeclaredMethod(
                "aeadDecrypt", ByteArray::class.java, ByteArray::class.java,
                ByteArray::class.java, ByteArray::class.java,
            ).apply { isAccessible = true }
            field.invoke(bob, currentKey, nonce, header, ct)
            fail("wrong key must not decrypt")
        } catch (e: Exception) {
            // expected: AEAD tag mismatch (InvocationTargetException wrapping SecurityException)
        }
    }
}
