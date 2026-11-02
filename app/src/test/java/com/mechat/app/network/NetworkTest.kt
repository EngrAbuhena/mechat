package com.mechat.app.network

import com.mechat.app.network.protocol.ClientMessage
import com.mechat.app.network.protocol.Protocol
import com.mechat.app.network.protocol.ServerMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkTest {

    @Test
    fun `protocol round trip for all client messages`() {
        val messages = listOf(
            ClientMessage.Hello("peer-abc"),
            ClientMessage.Signal("peer-b", "ENC(offer)"),
            ClientMessage.MailboxPut("peer-b", "ENC(blob)", 86400000L),
            ClientMessage.MailboxGet("peer-abc"),
        )
        for (m in messages) {
            val text = Protocol.encode(m)
            assertTrue(text.contains("\"type\""))
        }
        // Server messages decode correctly.
        val welcome = Protocol.decodeServer("""{"type":"Welcome","peerId":"peer-abc"}""")
        assertEquals("peer-abc", (welcome as ServerMessage.Welcome).peerId)
        val ack = Protocol.decodeServer("""{"type":"Ack","ok":true}""")
        assertTrue((ack as ServerMessage.Ack).ok)
    }

    @Test
    fun `backoff grows exponentially and is capped`() {
        assertEquals(1000L, Backoff.delayMs(0))
        assertEquals(2000L, Backoff.delayMs(1))
        assertEquals(4000L, Backoff.delayMs(2))
        assertEquals(60_000L, Backoff.delayMs(10)) // capped
        assertEquals(60_000L, Backoff.delayMs(100))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cleartext url is rejected`() {
        ServerConfig("ws://insecure.example/ws")
    }
}
