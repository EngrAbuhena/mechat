package com.mechat.app.ui.addcontact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.di.LockEntryPoint
import com.mechat.app.domain.invite.InviteManager
import com.mechat.app.ui.components.GlassField
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.QrCode
import com.mechat.app.ui.components.RoundIconButton
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing
import dagger.hilt.android.EntryPointAccessors

@Composable
fun AddContactScreen(
    onClose: () -> Unit,
    onScan: () -> Unit = {},
) {
    val tokens = LocalMeChatTokens.current
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Add contact or group",
                style = MaterialTheme.typography.titleLarge,
                color = tokens.textPrimary,
                modifier = Modifier.padding(vertical = Spacing.lg),
            )
            GlassField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Name your contact",
            )
            // Code field with a Scan QR icon button inside (CAMERA requested only on tap).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassField(
                    value = code,
                    onValueChange = { code = it; error = null },
                    placeholder = "Contact code (enter or paste)",
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onScan) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR code",
                        tint = tokens.textSecondary,
                    )
                }
            }
            if (error != null) {
                Text(error!!, color = tokens.alert, style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { /* placeholder: create group — later step */ },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                ) {
                    Text("Create Group", color = tokens.onPrimary)
                }
                RoundIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onClose,
                    containerColor = tokens.glassSurface,
                )
            }
        }
    }
}

/** Shows our one-time invite as QR + copyable text (create invite flow). */
@Composable
fun InviteQrCard(inviteText: String) {
    val tokens = LocalMeChatTokens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        QrCode(inviteText)
        Text(
            text = inviteText,
            color = tokens.textSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddContactScreenPreview() {
    MeChatTheme { AddContactScreen(onClose = {}) }
}
