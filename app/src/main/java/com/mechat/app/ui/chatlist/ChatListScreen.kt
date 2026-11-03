package com.mechat.app.ui.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier\nimport androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.components.ChipVariant
import com.mechat.app.ui.components.FloatingNavBar
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.PillChip
import com.mechat.app.ui.components.RoundIconButton
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.PillRadius
import com.mechat.app.ui.theme.Sizes
import com.mechat.app.ui.theme.Spacing

private data class Conversation(val id: String, val name: String)

// Placeholder data — real data layer comes in a later step.
private val PlaceholderConversations = listOf(
    Conversation("1", "Alice"),
    Conversation("2", "Bob"),
    Conversation("3", "Design Team"),
)

@Composable
fun ChatListScreen(
    onConversationClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onContactsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val tokens = LocalMeChatTokens.current

    GradientBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top pill: our short public ID (grouped), tap to copy + refresh button.
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(PillRadius))
                        .background(tokens.glassSurface)
                        .clickable { /* copy ID to clipboard — later step */ }
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy public ID",
                        tint = tokens.textSecondary,
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = "AB3C · D9E2", // grouped short public ID, placeholder
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.textPrimary,
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    RoundIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh ID",
                        onClick = { },
                        iconColor = tokens.textSecondary,
                    )
                }
                // Status line + connection indicator: Online / Offline ONLY — never the device IP.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = Spacing.sm),
                ) {
                    val connState = com.mechat.app.network.ConnectionState.CONNECTED // bound to ConnectionManager.state in a later step
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (connState == com.mechat.app.network.ConnectionState.CONNECTED)
                                    tokens.success else tokens.alert
                            ),
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        text = if (connState == com.mechat.app.network.ConnectionState.CONNECTED) "Online" else "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (connState == com.mechat.app.network.ConnectionState.CONNECTED) tokens.success else tokens.textSecondary,
                    )
                }
                PillChip(
                    text = "Relay", // Direct / Relay / Mailbox
                    variant = ChipVariant.ACCENT,
                    modifier = Modifier.padding(top = Spacing.xs),
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(PlaceholderConversations, key = { it.id }) { convo ->
                        ConversationRow(
                            conversation = convo,
                            onClick = { onConversationClick(convo.id) },
                        )
                    }
                }
            }
            FloatingNavBar(
                onContactsClick = onContactsClick,
                onSettingsClick = onSettingsClick,
                onAddClick = onAddClick,
            )
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    unread: Int = 0,
    onClick: () -> Unit,
) {
    val tokens = LocalMeChatTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PillRadius))
            .background(tokens.glassSurface)
            .clickable(onClick = onClick)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.avatar)
                .clip(CircleShape)
                .background(tokens.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = conversation.name.first().toString(),
                color = tokens.onPrimary,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = conversation.name,
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.textPrimary,
            modifier = Modifier.weight(1f),
        )
        RoundIconButton(
            icon = Icons.Default.Call,
            contentDescription = "Voice call ${conversation.name}",
            onClick = { },
            iconColor = tokens.textSecondary,
        )
        RoundIconButton(
            icon = Icons.Default.Videocam,
            contentDescription = "Video call ${conversation.name}",
            onClick = { },
            iconColor = tokens.textSecondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatListScreenPreview() {
    MeChatTheme { ChatListScreen({}, {}, {}) }
}
