package com.mechat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Sizes
import com.mechat.app.ui.theme.Spacing

/** 6-dot PIN indicator. Filled dots use the accent token. */
@Composable
fun PinDots(
    filledCount: Int,
    modifier: Modifier = Modifier,
    total: Int = 6,
) {
    val tokens = LocalMeChatTokens.current
    Row(
        modifier = modifier.padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Sizes.pinDotSpacing),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(Sizes.pinDot)
                    .clip(CircleShape)
                    .background(
                        if (index < filledCount) tokens.accent else tokens.textDisabled
                    ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PinDotsPreview() {
    MeChatTheme {
        GradientBackground {
            PinDots(filledCount = 4)
        }
    }
}
