package com.mechat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.PillRadius
import com.mechat.app.ui.theme.Spacing

enum class ChipVariant { ACCENT, ALERT, NEUTRAL }

/** Pill chip with accent / alert / neutral variants. */
@Composable
fun PillChip(
    text: String,
    variant: ChipVariant = ChipVariant.NEUTRAL,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalMeChatTokens.current
    val (bg, fg) = when (variant) {
        ChipVariant.ACCENT -> tokens.accent to tokens.onAccent
        ChipVariant.ALERT -> tokens.alert to Color.White
        ChipVariant.NEUTRAL -> tokens.glassSurface to tokens.textSecondary
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(PillRadius))
            .background(bg)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
    ) {
        Text(text = text, color = fg, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun PillChipPreview() {
    MeChatTheme {
        GradientBackground {
            Box(Modifier.padding(Spacing.lg)) {
                PillChip("Direct", ChipVariant.ACCENT)
            }
        }
    }
}
