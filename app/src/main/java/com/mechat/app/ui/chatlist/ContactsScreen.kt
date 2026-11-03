package com.mechat.app.ui.chatlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import com.mechat.app.data.db.Contact
import com.mechat.app.data.repository.ContactRepository
import com.mechat.app.di.ContactsEntryPoint
import com.mechat.app.ui.components.GlassCard
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.ListItem
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.Spacing
import dagger.hilt.android.EntryPointAccessors

/** Contact list with rename (local nickname), block and delete. */
@Composable
fun ContactsScreen() {
    val tokens = LocalMeChatTokens.current
    val context = LocalContext.current
    val repo: ContactRepository = remember {
        EntryPointAccessors.fromApplication(context, ContactsEntryPoint::class.java).contactRepository()
    }
    val contacts by repo.observeAll().collectAsState(initial = emptyList())
    var selected by remember { mutableStateOf<Contact?>(null) }

    GradientBackground {
        Column(Modifier.padding(Spacing.lg)) {
            Text(
                "Contacts",
                style = MaterialTheme.typography.titleLarge,
                color = tokens.textPrimary,
                modifier = Modifier.padding(vertical = Spacing.md),
            )
            GlassCard(Modifier.fillMaxWidth()) {
                LazyColumn {
                    items(contacts, key = { it.id }) { contact ->
                        ListItem(
                            label = contact.displayName + if (contact.blocked) " (blocked)" else "",
                            onClick = { selected = contact },
                        )
                    }
                }
            }
        }
        selected?.let { contact ->
            ContactActionsDialog(
                contact = contact,
                onDismiss = { selected = null },
                onRename = { name -> kotlinx.coroutines.runBlocking { repo.upsert(contact.copy(displayName = name)) } },
                onBlock = { block -> kotlinx.coroutines.runBlocking { repo.upsert(contact.copy(blocked = block)) } },
                onDelete = { kotlinx.coroutines.runBlocking { repo.delete(contact.id) } },
            )
        }
    }
}

@Composable
private fun ContactActionsDialog(
    contact: Contact,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onBlock: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    var newName by remember { mutableStateOf(contact.displayName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(contact.displayName) },
        text = {
            Column {
                com.mechat.app.ui.components.GlassField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = "Local nickname",
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                    TextButton(onClick = { onBlock(!contact.blocked); onDismiss() }) {
                        Text(if (contact.blocked) "Unblock" else "Block")
                    }
                    TextButton(onClick = { onDelete(); onDismiss() }) { Text("Delete") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onRename(newName); onDismiss() }) { Text("Save") }
        },
    )
}
