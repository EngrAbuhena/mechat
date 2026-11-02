package com.mechat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Sizes

/** Circular icon button, >= 48dp touch target, content description required. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    iconColor: Color = LocalMeChatTokens.current.textPrimary,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(Sizes.iconButton)
            .clip(CircleShape)
            .background(containerColor),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoundIconButtonPreview() {
    MeChatTheme {
        Box(
            modifier = Modifier.size(Sizes.iconButton + Sizes.sm),
            contentAlignment = Alignment.Center,
        ) {
            Text("●") // preview placeholder; real icons come from Material icons in Part B
        }
    }
}
