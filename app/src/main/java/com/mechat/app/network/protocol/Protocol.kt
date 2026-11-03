package com.mechat.app.network.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Typed wire protocol (kotlinx.serialization). All payloads are opaque to the
 * server: signalling data and mailbox blobs are E2E-encrypted client-side.
 */
@Serializable
sealed class ClientMessage {
    @Serializable data class Hello(val peerId: String) : ClientMessage()
    @Serializable data class Signal(val to: String, val data: String) : ClientMessage() // offer/answer/ICE, encrypted
    @Serializable data class MailboxPut(val to: String, val blob: String, val ttlMs: Long) : ClientMessage()
    @Serializable data class MailboxGet(val peerId: String) : ClientMessage()
}

@Serializable
sealed class ServerMessage {
    @Serializable data class Welcome(val peerId: String) : ServerMessage()
    @Serializable data class Signal(val from: String, val data: String) : ServerMessage()
    @Serializable data class Mailbox(val blob: String) : ServerMessage()
    @Serializable data class Ack(val ok: Boolean) : ServerMessage()
}

object Protocol {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun encode(msg: ClientMessage): String = json.encodeToString(ClientMessage.serializer(), msg)
    fun decodeServer(text: String): ServerMessage = json.decodeFromString(ServerMessage.serializer(), text)
}
