package com.mechat.app.data.repository

import com.mechat.app.data.db.AttachmentDao
import com.mechat.app.data.db.Contact
import com.mechat.app.data.db.ContactDao
import com.mechat.app.data.db.Conversation
import com.mechat.app.data.db.ConversationDao
import com.mechat.app.data.db.Message
import com.mechat.app.data.db.MessageDao
import com.mechat.app.data.db.SettingDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(private val dao: ContactDao) {
    fun observeAll(): Flow<List<Contact>> = dao.observeAll()
    suspend fun upsert(contact: Contact) = dao.upsert(contact)
    suspend fun rename(id: String, name: String) = dao.rename(id, name)
    suspend fun setBlocked(id: String, blocked: Boolean) = dao.setBlocked(id, blocked)
    suspend fun delete(id: String) = dao.delete(id)
}

@Singleton
class ConversationRepository @Inject constructor(private val dao: ConversationDao) {
    fun observeAll(): Flow<List<Conversation>> = dao.observeAll()
    suspend fun clearUnread(id: String) = dao.clearUnread(id)
    suspend fun bumpUnread(id: String, now: Long) = dao.bumpUnread(id, now)
    suspend fun upsert(conversation: Conversation) = dao.upsert(conversation)
}

@Singleton
class MessageRepository @Inject constructor(private val dao: MessageDao) {
    fun observeByConversation(conversationId: String): Flow<List<Message>> =
        dao.observeByConversation(conversationId)
    suspend fun upsert(message: Message) = dao.upsert(message)
    suspend fun deleteById(id: String) = dao.deleteById(id)
    suspend fun deleteExpired(now: Long) = dao.deleteExpired(now)
}

@Singleton
class SettingsRepository @Inject constructor(private val dao: SettingDao) {
    suspend fun get(key: String): String? = dao.get(key)
    suspend fun put(key: String, value: String) = dao.put(key, value)
}
