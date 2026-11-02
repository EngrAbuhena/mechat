package com.mechat.app.crypto.files

import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.LibraryLoader
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test

class FileCipherTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun load() { LibraryLoader.loadSharedLibraryIfNeeded() }
    }

    private val sodium = SodiumJava()

    @Test
    fun `chunked encrypt decrypt round trip across multiple chunks`() {
        val key = FileCipher.newFileKey()
        val data = ByteArray(FileCipher.CHUNK_SIZE * 2 + 123) { (it % 251).toByte() }
        val blob = FileCipher.encrypt(sodium, key, data)
        assertArrayEquals(data, FileCipher.decrypt(sodium, key, blob))
    }

    @Test
    fun `corrupted chunk is detected`() {
        val key = FileCipher.newFileKey()
        val blob = FileCipher.encrypt(sodium, key, "hello file".toByteArray())
        blob[blob.size - 1] = (blob[blob.size - 1] + 1).toByte()
        try { FileCipher.decrypt(sodium, key, blob); fail("corruption must be detected") }
        catch (e: SecurityException) {}
    }

    @Test
    fun `wrong key is rejected`() {
        val blob = FileCipher.encrypt(sodium, FileCipher.newFileKey(), "data".toByteArray())
        try { FileCipher.decrypt(sodium, FileCipher.newFileKey(), blob); fail() }
        catch (e: SecurityException) {}
    }
}
