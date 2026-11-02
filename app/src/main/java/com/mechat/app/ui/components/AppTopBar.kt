package com.mechat.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing

/** Rounded glass top bar with a centered title. */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalMeChatTokens.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    color = tokens.textPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    MeChatTheme {
        GradientBackground {
            AppTopBar("MeChat")
        }
    }
}
