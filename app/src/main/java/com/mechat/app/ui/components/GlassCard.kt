package com.mechat.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mechat.app.ui.theme.CardRadius
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing

/**
 * Glass card: surface token at low alpha with a thin border token.
 * Blur only where supported (backdrop blur is applied by callers on S+);
 * otherwise plain alpha. Radius from theme (28dp).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tokens = LocalMeChatTokens.current
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CardRadius),
        colors = CardDefaults.cardColors(containerColor = tokens.glassSurface),
        border = BorderStroke(1.dp, tokens.glassBorder),
        content = { content() },
    )
}

@Preview(showBackground = true)
@Composable
private fun GlassCardPreview() {
    MeChatTheme {
        GradientBackground(Modifier.fillMaxSize()) {
            Box(Modifier.padding(Spacing.lg)) {
                GlassCard(Modifier.padding(Spacing.md)) {
                    Text(
                        text = "Glass card",
                        modifier = Modifier.padding(Spacing.lg),
                        color = LocalMeChatTokens.current.textPrimary,
                    )
                }
            }
        }
    }
}
