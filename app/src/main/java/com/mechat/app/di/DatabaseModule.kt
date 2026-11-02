package com.mechat.app.di

import android.content.Context
import androidx.room.Room
import com.mechat.app.data.db.DatabaseKeyProvider
import com.mechat.app.data.db.MeChatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider,
    ): MeChatDatabase {
        // Key is created BEFORE the database builder runs, and SQLCipher gets the raw
        // 32-byte key (SupportFactory with ByteArray = PRAGMA key "x'<hex>'", no KDF).
        val rawKey = keyProvider.getOrCreateRawKey()
        return Room.databaseBuilder(context, MeChatDatabase::class.java, "mechat.db")
            .openHelperFactory(SupportFactory(rawKey))
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides fun provideContactDao(db: MeChatDatabase) = db.contactDao()
    @Provides fun provideConversationDao(db: MeChatDatabase) = db.conversationDao()
    @Provides fun provideMessageDao(db: MeChatDatabase) = db.messageDao()
    @Provides fun provideAttachmentDao(db: MeChatDatabase) = db.attachmentDao()
    @Provides fun provideSettingDao(db: MeChatDatabase) = db.settingDao()
}
