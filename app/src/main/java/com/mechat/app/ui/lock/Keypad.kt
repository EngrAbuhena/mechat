package com.mechat.app.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Sizes
import com.mechat.app.ui.theme.Spacing

/** 3x4 keypad, design-system tokens only. Touch targets >= 48dp. */
@Composable
fun Keypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalMeChatTokens.current
    val rows = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
        listOf(null, 0, -1), // -1 = backspace
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                row.forEach { digit ->
                    when (digit) {
                        null -> Box(Modifier.size(Sizes.iconButton))
                        -1 -> KeypadKey(
                            label = "⌫",
                            description = "Backspace",
                            onClick = onBackspace,
                        )
                        else -> KeypadKey(
                            label = digit.toString(),
                            description = "Digit $digit",
                            onClick = { onDigit(digit) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(label: String, description: String, onClick: () -> Unit) {
    val tokens = LocalMeChatTokens.current
    Box(
        modifier = Modifier
            .size(Sizes.iconButton)
            .clip(CircleShape)
            .background(tokens.glassSurface)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = tokens.textPrimary,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KeypadPreview() {
    MeChatTheme { Keypad(onDigit = {}, onBackspace = {}) }
}
