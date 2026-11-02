package com.mechat.app.network.mailbox

import com.mechat.app.data.repository.MessageRepository
import com.mechat.app.network.Backoff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Offline sync: on start/reconnect, fetch our mailbox, decrypt, store, ACK so the
 * server deletes each blob. Retry with backoff; dedupe by message ID so the same
 * message never appears twice.
 */
class OfflineSync(
    private val messages: MessageRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job()),
) {
    private val seen = ConcurrentHashMap.newKeySet<String>() // dedupe by message ID
    private var attempt = 0

    fun onReconnect(
        ourMailboxId: String,
        fetchBlobs: suspend () -> List<Pair<String, ByteArray>>, // (messageId, envelope)
        decryptStore: suspend (id: String, envelope: ByteArray) -> Boolean, // false if dup
        ack: suspend (messageId: String) -> Unit,
    ) {
        scope.launch {
            while (true) {
                try {
                    for ((id, envelope) in fetchBlobs()) {
                        if (seen.add(id)) {           // dedupe: never twice
                            if (decryptStore(id, envelope)) Unit
                            ack(id)                   // server deletes blob after ACK
                        }
                    }
                    attempt = 0
                    return@launch
                } catch (e: Exception) {
                    delay(Backoff.delayMs(attempt++)) // retry with backoff
                }
            }
        }
    }
}
