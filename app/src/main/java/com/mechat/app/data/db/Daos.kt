package com.mechat.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY displayName") fun observeAll(): Flow<List<Contact>>
    @Query("SELECT * FROM contacts WHERE id = :id") suspend fun get(id: String): Contact?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(contact: Contact)
    @Query("UPDATE contacts SET displayName = :name WHERE id = :id") suspend fun rename(id: String, name: String)
    @Query("UPDATE contacts SET blocked = :blocked WHERE id = :id") suspend fun setBlocked(id: String, blocked: Boolean)
    @Query("DELETE FROM contacts WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM contacts") suspend fun deleteAll()
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC") fun observeAll(): Flow<List<Conversation>>
    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id") suspend fun clearUnread(id: String)
    @Query("UPDATE conversations SET unreadCount = unreadCount + 1, lastMessageAt = :now WHERE id = :id") suspend fun bumpUnread(id: String, now: Long)
    @Query("DELETE FROM conversations WHERE id = :id") suspend fun delete(id: String)
    @Query("SELECT * FROM conversations WHERE id = :id") suspend fun get(id: String): Conversation?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(conversation: Conversation)
    @Query("DELETE FROM conversations") suspend fun deleteAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp")
    fun observeByConversation(conversationId: String): Flow<List<Message>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(message: Message)
    @Query("DELETE FROM messages WHERE id = :id") suspend fun deleteById(id: String)
    @Query("DELETE FROM messages WHERE expiresAt IS NOT NULL AND expiresAt < :now")
    suspend fun deleteExpired(now: Long): Int
    @Query("DELETE FROM messages") suspend fun deleteAll()
}

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE messageId = :messageId") fun observe(messageId: String): Flow<List<Attachment>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(attachment: Attachment)
    @Query("DELETE FROM attachments") suspend fun deleteAll()
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings") fun observeAll(): Flow<List<Setting>>
    @Query("SELECT value FROM settings WHERE key = :key") suspend fun get(key: String): String?
    @Query("INSERT OR REPLACE INTO settings(key, value) VALUES(:key, :value)")
    suspend fun put(key: String, value: String)
    @Query("DELETE FROM settings") suspend fun deleteAll()
}
