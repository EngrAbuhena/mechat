package com.mechat.app.domain.lock

import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.LibraryLoader
import com.mechat.app.crypto.PinHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

class LockTests {

    companion object {
        @BeforeClass
        @JvmStatic
        fun load() { LibraryLoader.loadSharedLibraryIfNeeded() }
    }

    private val hasher = PinHasher(SodiumJava())

    @Test
    fun `argon2id hash verifies correct pin and rejects wrong pin`() {
        val stored = hasher.hash("123456")
        assertTrue(hasher.verify("123456", stored))
        assertFalse(hasher.verify("123457", stored))
    }

    @Test
    fun `salt is random per hash`() {
        val a = hasher.hash("123456")
        val b = hasher.hash("123456")
        assertNotEquals(a.salt.toList(), b.salt.toList())
        assertNotEquals(a.hash.toList(), b.hash.toList())
    }

    @Test
    fun `pin length bounds enforced`() {
        assertFalse(hasher.isValidPinLength("12345"))
        assertTrue(hasher.isValidPinLength("123456"))
        assertTrue(hasher.isValidPinLength("123456789012"))
        assertFalse(hasher.isValidPinLength("1234567890123"))
        assertFalse(hasher.isValidPinLength("12a456"))
    }

    @Test
    fun `lockout after 5 failures then increasing delays`() {
        var state = LockoutPolicy.State()
        val t0 = 1_000_000L
        repeat(4) { state = LockoutPolicy.onFailure(state, t0) }
        assertEquals(0L, state.lockoutUntil) // not locked out yet
        state = LockoutPolicy.onFailure(state, t0)
        assertEquals(t0 + LockoutPolicy.FIRST_LOCKOUT_MS, state.lockoutUntil) // 15 min
        assertTrue(LockoutPolicy.remainingLockout(state, t0 + 1000) > 0)
        state = LockoutPolicy.onFailure(state, t0 + LockoutPolicy.FIRST_LOCKOUT_MS + 1)
        assertEquals(
            t0 + LockoutPolicy.FIRST_LOCKOUT_MS + 1 + 2 * LockoutPolicy.FIRST_LOCKOUT_MS,
            state.lockoutUntil,
        ) // doubled
    }

    @Test
    fun `success resets counters`() {
        var state = LockoutPolicy.State(failures = 3, lockoutUntil = 999L)
        state = LockoutPolicy.onSuccess()
        assertEquals(0, state.failures)
        assertEquals(0L, state.lockoutUntil)
    }
}
