package com.mechat.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Sizes
import com.mechat.app.ui.theme.Spacing

/** Grouped list row: icon, label, chevron. Touch target >= 48dp. */
@Composable
fun ListItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
) {
    val tokens = LocalMeChatTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = tokens.textSecondary,
                    modifier = Modifier.size(Sizes.iconButton / 2),
                )
            }
            Text(
                text = label,
                color = tokens.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = Spacing.sm),
            )
        }
        Text(text = "›", color = tokens.textSecondary)
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    MeChatTheme {
        GradientBackground {
            GlassCard {
                ListItem(label = "Identity", onClick = {})
            }
        }
    }
}
