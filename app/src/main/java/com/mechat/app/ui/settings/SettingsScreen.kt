package com.mechat.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.di.CryptoEntryPoint
import dagger.hilt.EntryPointAccessors
import com.mechat.app.ui.components.GlassCard
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.ListItem
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing

private val SettingsItems = listOf(
    "Identity",
    "Privacy & Security",
    "Messages",
    "Network",
    "Data & Storage",
    "About",
)

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val tokens = LocalMeChatTokens.current

    GradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = tokens.textPrimary,
                    modifier = Modifier.padding(vertical = Spacing.md),
                )
                GlassCard(Modifier.fillMaxWidth()) {
                    Column {
                        SettingsItems.forEach { item ->
                            ListItem(
                                label = item,
                                onClick = { /* placeholder — later step */ },
                                icon = Icons.Default.Info,
                                iconContentDescription = null,
                            )
                        }
                    }
                }
                // Disappearing messages (default 24h) + honesty note + screenshot protection.
                GlassCard(Modifier.fillMaxWidth().padding(top = Spacing.md)) {
                    Column(Modifier.padding(Spacing.md)) {
                        ListItem(label = "Disappearing messages (default 24h)", onClick = { })
                        Text(
                            text = "Disappearing messages cannot stop the other person from taking screenshots or copying.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Block screenshots (FLAG_SECURE)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = tokens.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(checked = true, onCheckedChange = { /* default ON */ })
                        }
                    }
                }
                // Always relay calls via TURN (hides your IP from the contact). Default OFF.
                GlassCard(Modifier.fillMaxWidth().padding(top = Spacing.md)) {
                    Column(Modifier.padding(Spacing.md)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Always relay calls (TURN)", style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                                Text(
                                    "Hides your IP from the contact, but the TURN server sees the relayed traffic. Default OFF (direct P2P).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.textSecondary,
                                )
                            }
                            Switch(checked = false, onCheckedChange = { })
                        }
                    }
                }
                // Panic PIN + auto-wipe controls.
                GlassCard(Modifier.fillMaxWidth().padding(top = Spacing.md)) {
                    Column(Modifier.padding(Spacing.md)) {
                        ListItem(label = "Set panic PIN", onClick = { showPanicDialog = true })
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Wipe after 10 wrong attempts",
                                style = MaterialTheme.typography.bodyLarge,
                                color = tokens.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = autoWipe,
                                onCheckedChange = {
                                    autoWipe = it
                                    lockManager.setAutoWipeEnabled(it)
                                },
                            )
                        }
                    }
                }
                if (showPanicDialog) {
                    AlertDialog(
                        onDismissRequest = { showPanicDialog = false },
                        title = { Text("Set panic PIN", color = tokens.textPrimary) },
                        text = {
                            Column {
                                GlassField(
                                    value = panicPin,
                                    onValueChange = { panicPin = it.filter(Char::isDigit).take(12) },
                                    placeholder = "Panic PIN (6–12 digits)",
                                )
                                if (panicError != null) Text(
                                    panicError!!,
                                    color = tokens.alert,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (lockManager.setPanicPin(panicPin)) {
                                    showPanicDialog = false
                                } else {
                                    panicError = "Must differ from your main PIN"
                                }
                            }) { Text("Save", color = tokens.primary) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPanicDialog = false }) {
                                Text("Cancel", color = tokens.textSecondary)
                            }
                        },
                    )
                }
                // Destructive action in its own alert-colored card.
                GlassCard(Modifier.fillMaxWidth().padding(top = Spacing.md)) {
                    ListItem(
                        label = "Reset identity",
                        onClick = { /* placeholder — later step */ },
                    )
                }
            }
            // App version in the bottom corner.
            Text(
                text = "MeChat 0.1.0",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.md),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MeChatTheme { SettingsScreen(onBack = {}) }
}
