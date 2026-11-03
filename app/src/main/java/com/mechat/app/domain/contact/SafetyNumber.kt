package com.mechat.app.domain.contact

import com.goterl.lazysodium.Sodium
import com.goterl.lazysodium.SodiumAndroid

/**
 * Safety number: SHA-256 of both public keys (sorted), shown as grouped digits
 * so two people can verify each other in person or by voice. Derived only from
 * public keys — no device identifiers.
 */
object SafetyNumber {

    fun of(ourPublicKey: ByteArray, theirPublicKey: ByteArray, sodium: com.goterl.lazysodium.LazySodium = com.goterl.lazysodium.LazySodium(SodiumAndroid())): String {
        val sorted = listOf(ourPublicKey, theirPublicKey).sortedWith { a, b ->
            a.compareTo(b)
        }
        val hash = sodium.cryptoHashSha256(sorted[0] + sorted[1])
        val digits = hash.joinToString("") { "%02d".format(it.toInt() and 0xFF) }
        return digits.chunked(5).joinToString(" ")
    }
}
