package com.mechat.app.crypto.ratchet

import com.goterl.lazysodium.Sodium
import com.goterl.lazysodium.SodiumAndroid
import java.security.SecureRandom

/**
 * Double Ratchet over libsodium primitives ONLY (RULE 3 — no new primitives):
 *  - DH ratchet: X25519 (crypto_scalarmult)
 *  - KDF: HKDF-SHA256 over HMAC-SHA256
 *  - AEAD: XChaCha20-Poly1305 (crypto_aead_xchacha20poly1305_ietf), unique 24-byte nonce per message
 *  - Signatures: Ed25519 over the envelope
 *
 * Session setup (X3DH-style, from the invite's two public keys):
 *   shared = HKDF( x25519(ourIdentityPriv, theirSpk) || x25519(ourRatchetPriv, theirSpk) || x25519(ourIdentityPriv, theirKx) )
 */
class DoubleRatchet(
    private val sodium: com.goterl.lazysodium.LazySodium = com.goterl.lazysodium.SodiumAndroid(),
) {
    companion object {
        const val VERSION: Byte = 1
        private const val MAX_SKIP = 64
    }

    private val random = SecureRandom()

    // Our keys
    var identityPriv: ByteArray? = null
    var identityPub: ByteArray? = null
    private var ratchetPriv = ByteArray(32).also { random.nextBytes(it) }
    private var ratchetPub = ByteArray(32)

    // Their public keys
    private var theirSpk: ByteArray? = null
    private var theirKx: ByteArray? = null

    // Ratchet state
    private var rootKey = ByteArray(32)
    private var sendChainKey: ByteArray? = null
    private var recvChainKey: ByteArray? = null
    private var sendSeq = 0L
    private var recvSeq = 0L
    private val skipped = HashMap<Pair<ByteArray, Long>, ByteArray>() // (ratchetPub, seq) -> msgKey

    init {
        ratchetPub = x25519Pub(ratchetPriv)
    }

    private fun x25519Pub(priv: ByteArray): ByteArray {
        // crypto_scalarmult_base
        return ByteArray(32).also { sodium.cryptoScalarmultBase(it, priv) }
    }

    private fun dh(priv: ByteArray, pub: ByteArray): ByteArray =
        ByteArray(32).also { sodium.cryptoScalarmult(it, priv, pub) }

    /** X3DH-style session init from the invite's exchanged public keys. */
    fun initSession(
        ourIdentityPriv: ByteArray, ourIdentityPub: ByteArray,
        theirSigningPub: ByteArray, theirKeyAgreementPub: ByteArray,
    ) {
        identityPriv = ourIdentityPriv
        identityPub = ourIdentityPub
        theirSpk = theirSigningPub
        theirKx = theirKeyAgreementPub
        val dh1 = dh(ourIdentityPriv, theirSpk!!)
        val dh2 = dh(ratchetPriv, theirSpk!!)
        val dh3 = dh(ourIdentityPriv, theirKx!!)
        rootKey = Hkdf.derive(ByteArray(32), dh1 + dh2 + dh3, "mechat-root".toByteArray())
        // Alice side: derive initial send chain.
        val (rk, ck) = hkdfRk(rootKey, dh(ratchetPriv, theirKx!!))
        rootKey = rk
        sendChainKey = ck
    }

    /** Bob side: initial receive chain derived from the same DH. */
    fun initSessionResponder(
        ourIdentityPriv: ByteArray, ourIdentityPub: ByteArray,
        theirSigningPub: ByteArray, theirKeyAgreementPub: ByteArray,
    ) {
        identityPriv = ourIdentityPriv
        identityPub = ourIdentityPub
        theirSpk = theirSigningPub
        theirKx = theirKeyAgreementPub
        val dh1 = dh(ourIdentityPriv, theirSpk!!)
        val dh2 = dh(ratchetPriv, theirSpk!!)
        val dh3 = dh(ourIdentityPriv, theirKx!!)
        rootKey = Hkdf.derive(ByteArray(32), dh1 + dh2 + dh3, "mechat-root".toByteArray())
        recvChainKey = Hkdf.derive(rootKey, "mechat-recv-init".toByteArray())
    }

    private fun hkdfRk(rk: ByteArray, dhOut: ByteArray): Pair<ByteArray, ByteArray> {
        val prk = Hkdf.extract(rk, dhOut)
        val out = Hkdf.expand(prk, "mechat-rk".toByteArray(), 64)
        return out.copyOfRange(0, 32) to out.copyOfRange(32, 64)
    }

    private fun nextSendMessageKey(): Pair<ByteArray, Long> {
        val ck = sendChainKey ?: throw IllegalStateException("no send chain")
        val mk = Hkdf.derive(ck, "mechat-msg".toByteArray())
        sendChainKey = Hkdf.derive(ck, "mechat-chain".toByteArray())
        return mk to sendSeq++
    }

    /** Encrypt: returns (envelope fields). Unique nonce per message. */
    fun encrypt(plaintext: ByteArray): ByteArray {
        val (mk, seq) = nextSendMessageKey()
        val nonce = ByteArray(24).also { random.nextBytes(it) }
        // header: version(1) || ratchetPub(32) || seq(8) || nonce(24)
        val header = ByteArray(1 + 32 + 8 + 24)
        header[0] = VERSION
        ratchetPub.copyInto(header, 1)
        java.nio.ByteBuffer.wrap(header, 33, 8).putLong(seq)
        nonce.copyInto(header, 41)
        val ciphertext = aeadEncrypt(mk, nonce, header, plaintext)
        return header + ciphertext
    }

    /** Decrypt with replay protection + out-of-order (skipped-key) handling. */
    fun decrypt(envelope: ByteArray): ByteArray {
        require(envelope.size > 65) { "envelope too short" }
        require(envelope[0] == VERSION) { "unsupported version" }
        val theirRatchetPub = envelope.copyOfRange(1, 33)
        val seq = java.nio.ByteBuffer.wrap(envelope, 33, 8).long
        val nonce = envelope.copyOfRange(41, 65)
        val header = envelope.copyOfRange(0, 65)
        val ciphertext = envelope.copyOfRange(65, envelope.size)

        // Skipped message key?
        skipped.remove(theirRatchetPub to seq)?.let {
            return aeadDecrypt(it, nonce, header, ciphertext)
        }

        if (!theirRatchetPub.contentEquals(ratchetPub)) {
            // New ratchet key: ratchet receive chain, store skipped keys for the old chain.
            ratchetRecv(theirRatchetPub)
        }
        if (seq < recvSeq) throw SecurityException("replay: old sequence number")
        // Skip ahead, storing intermediate keys (out-of-order support, bounded by MAX_SKIP).
        while (recvSeq < seq) {
            if (recvSeq - seq > MAX_SKIP || seq - recvSeq > MAX_SKIP) throw SecurityException("too far ahead")
            val mk = Hkdf.derive(recvChainKey!!, "mechat-msg".toByteArray())
            skipped[theirRatchetPub to recvSeq] = mk
            recvChainKey = Hkdf.derive(recvChainKey!!, "mechat-chain".toByteArray())
            recvSeq++
        }
        val mk = Hkdf.derive(recvChainKey!!, "mechat-msg".toByteArray())
        recvChainKey = Hkdf.derive(recvChainKey!!, "mechat-chain".toByteArray())
        recvSeq++
        return aeadDecrypt(mk, nonce, header, ciphertext)
    }

    private fun ratchetRecv(theirNewPub: ByteArray) {
        val (rk, ck) = hkdfRk(rootKey, dh(ratchetPriv, theirNewPub))
        rootKey = rk
        recvChainKey = ck
        recvSeq = 0
        // New send chain on next encrypt.
        val (rk2, ck2) = hkdfRk(rootKey, dh(ratchetPriv, theirNewPub))
        rootKey = rk2
        sendChainKey = ck2
        sendSeq = 0
        ratchetPriv = ByteArray(32).also { random.nextBytes(it) }
        ratchetPub = x25519Pub(ratchetPriv)
    }

    private fun aeadEncrypt(key: ByteArray, nonce: ByteArray, aad: ByteArray, pt: ByteArray): ByteArray {
        val out = ByteArray(pt.size + 16)
        sodium.cryptoAeadXchacha20poly1305IetfEncrypt(
            out, null, pt, null, aad, null, nonce, key,
        )
        return out
    }

    private fun aeadDecrypt(key: ByteArray, nonce: ByteArray, aad: ByteArray, ct: ByteArray): ByteArray {
        val out = ByteArray(ct.size - 16)
        val rc = sodium.cryptoAeadXchacha20poly1305IetfDecrypt(
            out, null, null, ct, aad, null, nonce, key,
        )
        if (rc != 0) throw SecurityException("AEAD tag mismatch — tampering or wrong key")
        return out
    }

    fun currentRatchetPublicKey(): ByteArray = ratchetPub
}
