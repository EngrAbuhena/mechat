package com.mechat.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.PillRadius
import com.mechat.app.ui.theme.Sizes

/** Pill-shaped glass text field. Fully rounded, >= 48dp tall touch target. */
@Composable
fun GlassField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
) {
    val tokens = LocalMeChatTokens.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.touchTargetMin),
        placeholder = { Text(placeholder, color = tokens.textSecondary) },
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        shape = RoundedCornerShape(PillRadius),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = tokens.glassSurface,
            unfocusedContainerColor = tokens.glassSurface,
            disabledContainerColor = tokens.glassSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = tokens.textPrimary,
            unfocusedTextColor = tokens.textPrimary,
        ),
        border = BorderStroke(Sizes.borderThin, tokens.glassBorder),
    )
}

@Preview(showBackground = true)
@Composable
private fun GlassFieldPreview() {
    MeChatTheme {
        var text by remember { mutableStateOf("") }
        GradientBackground {
            GlassField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Your message",
            )
        }
    }
}
