package com.mechat.app.domain.contact

import com.goterl.lazysodium.SodiumAndroid

/**
 * Safety number: SHA-256 of both public keys (sorted), shown as grouped digits
 * so two people can verify each other in person or by voice. Derived only from
 * public keys — no device identifiers.
 */
object SafetyNumber {

    fun of(ourPublicKey: ByteArray, theirPublicKey: ByteArray, sodium: SodiumAndroid = SodiumAndroid()): String {
        val sorted = listOf(ourPublicKey, theirPublicKey).sortedWith { a, b ->
            java.util.Arrays.compareUnsigned(a, b)
        }
        val input = sorted[0] + sorted[1]
        val hash = ByteArray(32)
        sodium.crypto_hash_sha256(hash, input, input.size.toLong())
        val digits = hash.joinToString("") { "%02d".format(it.toInt() and 0xFF) }
        return digits.chunked(5).joinToString(" ")
    }
}
