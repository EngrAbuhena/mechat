package com.mechat.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.components.ChipVariant
import com.mechat.app.ui.components.GlassCard
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.PillChip
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing

/**
 * Debug screen: shows the current connection route honestly —
 * Direct (P2P), Relayed (TURN), or Mailbox (offline fallback).
 */
@Composable
fun RouteDebugScreen(route: String = "Direct") {
    val tokens = LocalMeChatTokens.current
    GradientBackground {
        Column(Modifier.padding(Spacing.lg)) {
            Text(
                "Connection route (debug)",
                style = MaterialTheme.typography.titleLarge,
                color = tokens.textPrimary,
                modifier = Modifier.padding(vertical = Spacing.md),
            )
            GlassCard {
                Column(Modifier.padding(Spacing.lg)) {
                    PillChip(
                        text = route,
                        variant = when (route) {
                            "Mailbox" -> ChipVariant.ALERT
                            "Relayed" -> ChipVariant.ACCENT
                            else -> ChipVariant.NEUTRAL
                        },
                    )
                    Text(
                        text = when (route) {
                            "Direct" -> "Peer-to-peer WebRTC data channel."
                            "Relayed" -> "Relayed via TURN server (server sees only ciphertext)."
                            else -> "Offline mailbox on the relay (end-to-end encrypted blob)."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = tokens.textSecondary,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RouteDebugScreenPreview() {
    MeChatTheme { RouteDebugScreen("Relayed") }
}
