package com.mechat.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Contact::class, Conversation::class, Message::class, Attachment::class, Setting::class],
    version = 1,
    exportSchema = false,
)
abstract class MeChatDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun settingDao(): SettingDao
}
