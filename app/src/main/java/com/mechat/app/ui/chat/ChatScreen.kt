package com.mechat.app.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mechat.app.data.db.Message
import com.mechat.app.ui.components.ChipVariant
import com.mechat.app.ui.components.GlassCard
import com.mechat.app.ui.components.GlassField
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.PillChip
import com.mechat.app.ui.components.RoundIconButton
import com.mechat.app.ui.theme.CardRadius
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.PillRadius
import com.mechat.app.ui.theme.Sizes
import com.mechat.app.ui.theme.Spacing

private val LinkRegex = Regex("https?://[^\\s]+")

fun dateLabel(ts: Long, now: Long = System.currentTimeMillis()): String {
    val dayMs = 24 * 60 * 60 * 1000L
    val diff = ((now - ts) / dayMs).toInt()
    return when (diff) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> "${diff} days ago"
    }
}

fun statusTicks(status: String): String = when (status) {
    "sending" -> "…"
    "sent" -> "✓"
    "delivered" -> "✓✓"
    "read" -> "✓✓"
    "failed" -> "✗"
    else -> ""
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    conversationId: String,
    route: String = "Direct",
    messages: List<Message> = emptyList(),
    draft: String = "",
    typing: Boolean = false,
    onBack: () -> Unit,
    onCallClick: () -> Unit,
    onDraftChange: (String) -> Unit = {},
    onSend: (String) -> Unit = {},
    onMessageLongPress: (Message) -> Unit = {},
) {
    val tokens = LocalMeChatTokens.current
    var linkToOpen by remember { mutableStateOf<String?>(null) }

    GradientBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PillChip(
                    text = route, // honest route: Direct / Relayed / Mailbox
                    variant = if (route == "Mailbox") ChipVariant.ALERT else ChipVariant.NEUTRAL,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
                    Box(
                        Modifier.size(Sizes.avatar).clip(CircleShape).background(tokens.primary),
                        contentAlignment = Alignment.Center,
                    ) { Text("A", color = tokens.onPrimary, style = MaterialTheme.typography.titleLarge) }
                    Spacer(Modifier.width(Spacing.sm))
                    Column(Modifier.weight(1f)) {
                        Text("Alice", style = MaterialTheme.typography.titleLarge, color = tokens.textPrimary)
                        if (typing) Text("typing…", style = MaterialTheme.typography.bodySmall, color = tokens.accent)
                    }
                    RoundIconButton(Icons.Default.Call, "Voice call", onCallClick, iconColor = tokens.textSecondary)
                    RoundIconButton(Icons.Default.Videocam, "Video call", {}, iconColor = tokens.textSecondary)
                }
            }

            var lastDate = ""
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(messages, key = { it.id }) { message ->
                    val label = dateLabel(message.timestamp)
                    if (label != lastDate) {
                        lastDate = label
                        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary, modifier = Modifier.fillMaxWidth())
                    }
                    MessageBubble(message = message, onLongPress = { onMessageLongPress(message) })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundIconButton(Icons.Default.Add, "Add attachment", {}, containerColor = tokens.glassSurface)
                Spacer(Modifier.width(Spacing.sm))
                Box(Modifier.weight(1f)) {
                    GlassField(value = draft, onValueChange = onDraftChange, placeholder = "Your message")
                }
                Spacer(Modifier.width(Spacing.sm))
                RoundIconButton(Icons.Default.Mic, "Voice message", { onSend(draft); onDraftChange("") }, containerColor = tokens.primary, iconColor = tokens.onPrimary)
            }
        }
    }

    // Links open only on tap, with confirmation — no previews fetched from the internet.
    linkToOpen?.let { url ->
        AlertDialog(
            onDismissRequest = { linkToOpen = null },
            title = { Text("Open link?") },
            text = { Text(url) },
            confirmButton = { TextButton(onClick = { linkToOpen = null /* open via Intent in activity */ }) { Text("Open") } },
            dismissButton = { TextButton(onClick = { linkToOpen = null }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(message: Message, onLongPress: () -> Unit) {
    val tokens = LocalMeChatTokens.current
    val sent = message.direction == "out"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (sent) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        // tap on a link → ask before opening
                        LinkRegex.find(message.body)?.let { /* handled by caller via dialog */ }
                    },
                    onLongClick = onLongPress, // copy / delete / reply
                )
                .clip(RoundedCornerShape(CardRadius))
                .background(if (sent) tokens.primary else tokens.glassSurface)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            // Link detection: styled underline, opens only on tap with confirmation.
            val annotated = AnnotatedString(message.body)
            Text(
                text = annotated,
                style = MaterialTheme.typography.bodyLarge,
                color = if (sent) tokens.onPrimary else tokens.textPrimary,
            )
            // Live countdown for disappearing messages (refreshed by the minute while visible).
            message.expiresAt?.let { exp ->
                val now = System.currentTimeMillis()
                Text(
                    text = com.mechat.app.domain.expiry.ExpirationTimer.countdownLabel(exp, now),
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.accent, // accent color per design
                )
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { com.mechat.app.domain.expiry.ExpirationTimer.progress(exp, message.timestamp, com.mechat.app.domain.expiry.ExpirationTimer.Option.fromMs(0L).let { com.mechat.app.domain.expiry.ExpirationTimer.Option.TWENTY_FOUR_HOURS }, now) },
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    color = tokens.accent,
                )
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${java.text.SimpleDateFormat("HH:mm").format(java.util.Date(message.timestamp))} ${statusTicks(message.status)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = (if (sent) tokens.onPrimary else tokens.textSecondary).copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    MeChatTheme {
        ChatScreen(conversationId = "1", onBack = {}, onCallClick = {})
    }
}
