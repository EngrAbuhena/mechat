package com.mechat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.CardRadius
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Sizes
import com.mechat.app.ui.theme.Spacing

/** Bottom floating nav bar: Contacts, Settings, plus a separate accent "+" button. */
@Composable
fun FloatingNavBar(
    onContactsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalMeChatTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            .clip(RoundedCornerShape(CardRadius))
            .background(tokens.glassSurface)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavButton(label = "Contacts", contentDescription = "Contacts", onClick = onContactsClick)
        NavButton(label = "Settings", contentDescription = "Settings", onClick = onSettingsClick)
        // separate accent "+" button
        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(Sizes.fab)
                .clip(CircleShape)
                .background(tokens.accent),
        ) {
            Text(
                text = "+",
                color = tokens.onAccent,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun NavButton(label: String, contentDescription: String, onClick: () -> Unit) {
    val tokens = LocalMeChatTokens.current
    IconButton(onClick = onClick, modifier = Modifier.size(Sizes.iconButton)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.first().toString(),
                color = tokens.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FloatingNavBarPreview() {
    MeChatTheme {
        GradientBackground {
            Box(Modifier.fillMaxWidth()) {
                FloatingNavBar({}, {}, {})
            }
        }
    }
}
