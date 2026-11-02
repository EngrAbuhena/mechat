package com.mechat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// DESIGN TOKENS — full theme state exposed to components.
// Dark-first: dark is the default; light follows system setting.
// ============================================================

data class MeChatTokens(
    val gradient: Brush,
    val background: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val onAccent: Color,
    val success: Color,
    val alert: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
)

val LocalMeChatTokens = staticCompositionLocalOf<MeChatTokens> {
    error("MeChatTokens not provided — wrap content in MeChatTheme")
}

private val DarkTokens = MeChatTokens(
    gradient = Brush.verticalGradient(
        colors = listOf(DarkGradientTop, DarkGradientMid, DarkGradientBottom),
    ),
    background = DarkGradientBottom,
    glassSurface = GlassSurfaceDark,
    glassBorder = GlassBorderDark,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    accent = DarkAccent,
    onAccent = DarkOnAccent,
    success = DarkSuccess,
    alert = DarkAlert,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textDisabled = DarkTextDisabled,
)

private val LightTokens = MeChatTokens(
    gradient = Brush.verticalGradient(colors = listOf(LightBackground, LightBackground)),
    background = LightBackground,
    glassSurface = GlassSurfaceLight,
    glassBorder = GlassBorderLight,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    accent = LightAccent,
    onAccent = LightOnAccent,
    success = LightSuccess,
    alert = LightAlert,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textDisabled = LightTextDisabled,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    surface = DarkGradientBottom,
    onSurface = DarkTextPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    surface = LightBackground,
    onSurface = LightTextPrimary,
)

@Composable
fun MeChatTheme(
    darkTheme: Boolean = true, // dark-first; Part B wires this to isSystemInDarkTheme()
    content: @Composable () -> Unit,
) {
    val tokens = if (darkTheme) DarkTokens else LightTokens
    CompositionLocalProvider(LocalMeChatTokens provides tokens) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
