package com.mechat.app.network.mailbox

import com.goterl.lazysodium.Sodium
import com.goterl.lazysodium.SodiumAndroid
import com.mechat.app.crypto.ratchet.Hkdf
import com.mechat.app.network.protocol.ClientMessage
import com.mechat.app.network.protocol.Protocol
import java.util.Base64

/**
 * Blind mailbox client. The mailbox ID is derived from the contact's public key +
 * a per-conversation secret (HKDF), so the server cannot link conversations to
 * each other or to identities. Blobs are opaque E2E ciphertext; the server never
 * sees plaintext. Sizes are padded to buckets to reduce size leakage.
 */
object Mailbox {

    private val sodium: Sodium = SodiumAndroid()

    /** Buckets: 256B / 1KiB / 4KiB (padding reduces size leakage). */
    fun padSize(len: Int): Int = when {
        len <= 256 -> 256
        len <= 1024 -> 1024
        else -> 4096
    }

    fun pad(blob: ByteArray): ByteArray {
        val target = padSize(blob.size)
        return blob + ByteArray(target - blob.size) // zero padding; AEAD tag inside blob
    }

    /** Mailbox ID for a conversation: HKDF(contactPubKey, conversationSecret). */
    fun mailboxId(contactPublicKey: ByteArray, conversationSecret: ByteArray): String {
        val id = Hkdf.derive(contactPublicKey, conversationSecret, "mechat-mailbox-id".toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id)
    }

    fun put(toMailboxId: String, envelope: ByteArray, ttlMs: Long): String =
        Protocol.encode(ClientMessage.MailboxPut(toMailboxId, Base64.getEncoder().encodeToString(pad(envelope)), ttlMs))

    fun get(ourMailboxId: String): String =
        Protocol.encode(ClientMessage.MailboxGet(ourMailboxId))

    fun unpad(blob: ByteArray, realLen: Int): ByteArray = blob.copyOfRange(0, realLen)
}
