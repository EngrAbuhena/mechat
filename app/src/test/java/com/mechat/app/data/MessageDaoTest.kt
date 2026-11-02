package com.mechat.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mechat.app.data.db.Conversation
import com.mechat.app.data.db.MeChatDatabase
import com.mechat.app.data.db.Message
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * DAO tests on an in-memory database. SQLCipher itself is exercised on-device with
 * the raw 32-byte test key; here we verify schema/DAO behaviour.
 */
@RunWith(RobolectricTestRunner::class)
class MessageDaoTest {

    private lateinit var db: MeChatDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, MeChatDatabase::class.java).build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `message round trip and expiry`() = runBlocking {
        db.conversationDao().upsert(Conversation("c1", null, "Alice", 1L))
        db.messageDao().upsert(Message("m1", "c1", "out", "hi", 100L, "sent", null))
        db.messageDao().upsert(Message("m2", "c1", "in", "hey", 200L, "delivered", 150L))

        val all = db.messageDao().observeByConversation("c1").first()
        assertEquals(2, all.size)

        val deleted = db.messageDao().deleteExpired(200L)
        assertEquals(1, deleted)
        assertTrue(db.messageDao().observeByConversation("c1").first().all { it.expiresAt == null })
    }
}
