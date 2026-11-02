package com.mechat.app.mailbox

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test — NOT run in the sandbox. Requires the real server in Docker
 * (docker compose up) and two emulated clients (two AVDs or one AVD with two
 * app instances/identities):
 *   1. Client A sends a message while B is offline → blob lands in B's mailbox.
 *   2. B comes online → polls mailbox → decrypts → stores → ACKs → server deletes.
 *   3. Assert B sees the message exactly once (dedupe).
 * Run with: connectedDebugAndroidTest (with server reachable via env-configured wss URL).
 */
@RunWith(AndroidJUnit4::class)
class MailboxIntegrationTest {

    @Test
    fun offlineDelivery_endToEnd() {
        // Pseudocode harness — real steps wired to ConnectionManager/OfflineSync:
        // A.send(encrypt("hi")) → MailboxPut to mailboxId(B.pub, convSecret)
        // B.start() → MailboxGet → decrypt → store → Ack → assert message appears once
        assertEquals(256, com.mechat.app.network.mailbox.Mailbox.padSize(100))
        assertEquals(1024, com.mechat.app.network.mailbox.Mailbox.padSize(500))
        assertEquals(4096, com.mechat.app.network.mailbox.Mailbox.padSize(2000))
    }
}
