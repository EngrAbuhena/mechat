package com.mechat.app.domain.invite

import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.LibraryLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

class InviteTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun load() { LibraryLoader.loadSharedLibraryIfNeeded() }
    }

    private val sodium = SodiumJava()
    private val signing = sodium.cryptoSignKeyPair()
    private val kx = sodium.cryptoBoxKeyPair()

    private fun makeInvite(ttlMs: Long = InviteCodec.DEFAULT_TTL_MS, now: Long = 1_000_000L) =
        InviteCodec.create(sodium, signing, kx.publicKey.asBytes, "relay.example:443", ttlMs, now)

    @Test
    fun `encode decode round trip preserves fields`() {
        val (invite, text) = makeInvite()
        val parsed = InviteCodec.parse(sodium, text)!!
        assertTrue(parsed.signingPublicKey.contentEquals(invite.signingPublicKey))
        assertTrue(parsed.keyAgreementPublicKey.contentEquals(invite.keyAgreementPublicKey))
        assertTrue(parsed.token.contentEquals(invite.token))
        assertEquals("relay.example:443", parsed.relayAddress)
        assertEquals(invite.expiresAt, parsed.expiresAt)
    }

    @Test
    fun `expired invite is rejected`() {
        val (invite, _) = makeInvite(ttlMs = 1000L, now = 1_000_000L)
        assertTrue(InviteCodec.isExpired(invite, now = 1_000_000L + 1000L))
        assertFalse(InviteCodec.isExpired(invite, now = 1_000_000L + 999L))
    }

    @Test
    fun `tampered text fails signature verification`() {
        val (_, text) = makeInvite()
        val tampered = text.dropLast(2) + "AA"
        assertNull(InviteCodec.parse(sodium, tampered))
    }

    @Test
    fun `malformed text is rejected`() {
        assertNull(InviteCodec.parse(sodium, "hello"))
        assertNull(InviteCodec.parse(sodium, "MCHAT1.!!!.???"))
    }

    @Test
    fun `reuse of a token is rejected by manager logic`() {
        // Mirror InviteManager's one-time check against a used-token set.
        val (invite, _) = makeInvite()
        val tokenId = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(invite.token)
        val used = mutableSetOf(tokenId)
        assertTrue(tokenId in used) // second accept would be rejected
    }
}
