package com.mechat.app.crypto.files

import com.goterl.lazysodium.SodiumAndroid
import java.security.SecureRandom

/**
 * Chunked streaming AEAD for attachments. Random 32-byte file key per file;
 * XChaCha20-Poly1305 per chunk with a unique 24-byte nonce (chunk index encoded in
 * the nonce). The file key travels inside the encrypted message envelope.
 * Format: [magic 4B "MCF1"][chunkCount 4B][chunk0: nonce24+ct+tag16]...
 */
object FileCipher {

    const val CHUNK_SIZE = 64 * 1024
    private val MAGIC = byteArrayOf('M'.code.toByte(), 'C'.code.toByte(), 'F'.code.toByte(), '1'.code.toByte())

    fun newFileKey(): ByteArray = ByteArray(32).also { SecureRandom().nextBytes(it) }

    private fun chunkNonce(index: Int): ByteArray {
        val n = ByteArray(24)
        java.nio.ByteBuffer.wrap(n, 16, 8).putLong(index.toLong())
        return n
    }

    fun encrypt(sodium: SodiumAndroid, key: ByteArray, plaintext: ByteArray): ByteArray {
        val chunkCount = (plaintext.size + CHUNK_SIZE - 1) / CHUNK_SIZE
        val out = ByteArray(8)
        MAGIC.copyInto(out, 0)
        java.nio.ByteBuffer.wrap(out, 4, 4).putInt(chunkCount)
        val chunks = ArrayList<ByteArray>()
        for (i in 0 until chunkCount) {
            val from = i * CHUNK_SIZE
            val to = minOf(from + CHUNK_SIZE, plaintext.size)
            val pt = plaintext.copyOfRange(from, to)
            val nonce = chunkNonce(i)
            val ct = ByteArray(pt.size + 16)
            val cLen = longArrayOf(0)
            sodium.crypto_aead_xchacha20poly1305_ietf_encrypt(
                ct, cLen, pt, pt.size.toLong(), null, 0, null, nonce, key,
            )
            chunks += nonce + ct
        }
        return out + chunks.reduce { a, b -> a + b }
    }

    /** Decrypts one chunk; throws on AEAD tag mismatch (corruption detection). */
    fun decryptChunk(sodium: SodiumAndroid, key: ByteArray, nonce: ByteArray, ct: ByteArray): ByteArray {
        val pt = ByteArray(ct.size - 16)
        val mLen = longArrayOf(0)
        val rc = sodium.crypto_aead_xchacha20poly1305_ietf_decrypt(
            pt, mLen, null, ct, ct.size.toLong(), null, 0, nonce, key,
        )
        if (rc != 0) throw SecurityException("chunk AEAD tag mismatch — corrupted file")
        return pt
    }

    fun decrypt(sodium: SodiumAndroid, key: ByteArray, blob: ByteArray): ByteArray {
        require(blob.size >= 8 && blob.copyOfRange(0, 4).contentEquals(MAGIC)) { "bad file format" }
        val chunkCount = java.nio.ByteBuffer.wrap(blob, 4, 4).int
        val out = ArrayList<ByteArray>()
        var pos = 8
        for (i in 0 until chunkCount) {
            val nonce = blob.copyOfRange(pos, pos + 24)
            val ct = blob.copyOfRange(pos + 24, blob.size.coerceAtMost(pos + 24 + CHUNK_SIZE + 16))
            out += decryptChunk(sodium, key, nonce, ct)
            pos += 24 + ct.size
        }
        return out.reduce { a, b -> a + b }
    }
}
