package com.mechat.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing

/** Centered empty-state message. */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalMeChatTokens.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = tokens.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
        )
        Text(
            text = subtitle,
            color = tokens.textSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    MeChatTheme {
        GradientBackground {
            EmptyState("No conversations yet", "Tap + to add a contact")
        }
    }
}
