package com.mechat.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: String,          // public ID derived from their signing key
    val displayName: String,             // local nickname only
    val signingPublicKey: ByteArray,
    val createdAt: Long,
    val blocked: Boolean = false,
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val contactId: String?,
    val title: String,
    val createdAt: Long,
    val lastMessageAt: Long = 0L,
    val unreadCount: Int = 0,
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String,
    val direction: String,   // "in" | "out"
    val body: String,
    val timestamp: Long,
    val status: String,      // "sending" | "sent" | "delivered" | "read" | "failed"
    val expiresAt: Long?,    // null = never; used for ephemeral messages
)

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("messageId")],
)
data class Attachment(
    @PrimaryKey val id: String,
    val messageId: String,
    val mimeType: String,
    val filePath: String,    // encrypted local file path
    val sizeBytes: Long,
)

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey val key: String,
    val value: String,
)
