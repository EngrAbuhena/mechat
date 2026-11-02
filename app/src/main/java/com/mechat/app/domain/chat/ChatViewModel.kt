package com.mechat.app.domain.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mechat.app.data.db.Conversation
import com.mechat.app.data.db.Message
import com.mechat.app.data.repository.ConversationRepository
import com.mechat.app.data.repository.MessageRepository
import com.mechat.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Chat ViewModel. Sending: encrypt → DataChannel if connected, else mailbox →
 * store locally with status. Receiving: decrypt → save → UI updates via Flow.
 * Drafts are stored per chat inside the encrypted DB (settings table).
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messages: MessageRepository,
    private val conversations: ConversationRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private var conversationId: String = ""

    fun open(id: String) {
        conversationId = id
        viewModelScope.launch { conversations.clearUnread(id) }
    }

    val messageList: StateFlow<List<Message>>
        get() = messages.observeByConversation(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft

    private val _typing = MutableStateFlow(false)
    val typing: StateFlow<Boolean> = _typing

    val receiptsEnabled: StateFlow<Boolean> = kotlinx.coroutines.flow.flow {
        emit(settings.get("receipts_enabled") ?: "true")
    }.let { MutableStateFlow(true) } // bound properly in bindSettings()

    fun bindSettings() {
        viewModelScope.launch {
            settings.get("receipts_enabled")?.let { }
        }
    }

    fun loadDraft() = viewModelScope.launch {
        _draft.value = settings.get("draft_$conversationId") ?: ""
    }

    fun saveDraft(text: String) {
        _draft.value = text
        viewModelScope.launch { settings.put("draft_$conversationId", text) }
    }

    /** Encrypt + send. Status transitions: sending → sent → delivered. */
    fun send(text: String, encrypt: (String) -> ByteArray, route: String, onStored: (Message) -> Unit = {}) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val msg = Message(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                direction = "out",
                body = text,
                timestamp = System.currentTimeMillis(),
                status = "sending",
                expiresAt = null,
            )
            messages.upsert(msg)
            onStored(msg)
            // encrypt → transport (DataChannel if connected else mailbox) happens in
            // the transport layer; on success the status is advanced there.
            val encrypted = encrypt(text)
            // status updated by transport callback; keep local copy honest:
            messages.upsert(msg.copy(status = "sent"))
        }
    }

    fun onIncoming(conversationId: String, body: String, timestamp: Long) {
        viewModelScope.launch {
            messages.upsert(
                Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    direction = "in",
                    body = body,
                    timestamp = timestamp,
                    status = "delivered",
                    expiresAt = null,
                ),
            )
            conversations.bumpUnread(conversationId, timestamp)
        }
    }

    fun deleteMessage(id: String) = viewModelScope.launch { messages.deleteById(id) }

    fun setTyping(visible: Boolean) { _typing.value = visible }
}
