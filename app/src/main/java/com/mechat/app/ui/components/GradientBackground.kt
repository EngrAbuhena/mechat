package com.mechat.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mechat.app.ui.theme.Alpha
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing

/**
 * App background: vertical gradient plus a subtle ORIGINAL pattern
 * (repeating rounded diagonal strokes) drawn in Canvas at <= 5% opacity.
 * No bitmap files.
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tokens = LocalMeChatTokens.current
    Box(modifier = modifier.background(tokens.gradient)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 56.dp.toPx()
            val stroke = 2.dp.toPx()
            var y = -size.height
            while (y < size.height * 2f) {
                drawLine(
                    color = tokens.textPrimary.copy(alpha = Alpha.backgroundPattern),
                    start = Offset(-size.width, y),
                    end = Offset(size.width * 2f, y + size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                y += spacing
            }
        }
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun GradientBackgroundPreview() {
    MeChatTheme {
        GradientBackground(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize())
        }
    }
}
