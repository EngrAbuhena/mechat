package com.mechat.app.crypto.ratchet

import com.goterl.lazysodium.Sodium
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * HKDF-SHA256 (RFC 5869) built from HMAC-SHA256 — a standard KDF construction over
 * libsodium's hash, not a new primitive (RULE 3).
 */
object Hkdf {

    fun extract(salt: ByteArray, ikm: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(salt, "HmacSHA256"))
        return mac.doFinal(ikm)
    }

    fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        val out = ByteArray(length)
        var t = ByteArray(0)
        var pos = 0
        var counter = 1
        while (pos < length) {
            mac.reset()
            mac.update(t)
            mac.update(info)
            mac.update(byteArrayOf(counter++.toByte()))
            t = mac.doFinal()
            val n = minOf(t.size, length - pos)
            t.copyInto(out, pos, 0, n)
            pos += n
        }
        return out
    }

    fun derive(salt: ByteArray, ikm: ByteArray, info: ByteArray = ByteArray(0), length: Int = 32) =
        expand(extract(salt, ikm), info, length)
}
