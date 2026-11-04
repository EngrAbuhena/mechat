package com.mechat.app.domain.contact

import com.goterl.lazysodium.SodiumAndroid

/**
 * Safety number: SHA-256 of both public keys (sorted), shown as grouped digits
 * so two people can verify each other in person or by voice. Derived only from
 * public keys — no device identifiers.
 */
object SafetyNumber {

    fun of(ourPublicKey: ByteArray, theirPublicKey: ByteArray, sodium: com.goterl.lazysodium.Sodium = SodiumAndroid()): String {
        val sorted = listOf(ourPublicKey, theirPublicKey).sortedWith { a, b ->
            compareUnsigned(a, b)
        }
        val input = sorted[0] + sorted[1]
        val hash = ByteArray(32)
        sodium.crypto_hash_sha256(hash, input, input.size.toLong())
        val digits = hash.joinToString("") { "%02d".format(it.toInt() and 0xFF) }
        return digits.chunked(5).joinToString(" ")
    }
    private fun compareUnsigned(a: ByteArray, b: ByteArray): Int {
        val n = minOf(a.size, b.size)
        for (i in 0 until n) {
            val x = a[i].toInt() and 0xFF
            val y = b[i].toInt() and 0xFF
            if (x != y) return x - y
        }
        return a.size - b.size
    }
}
