package com.mechat.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// DESIGN TOKENS — colors. The ONLY place colors are defined (RULE 7).
// Re-skin the app by editing this file only.
// ============================================================

// --- Dark theme (default, dark-first) ---
val DarkGradientTop = Color(0xFF08151F)
val DarkGradientMid = Color(0xFF0A2A2F)
val DarkGradientBottom = Color(0xFF0F1A1E)
val DarkPrimary = Color(0xFF2E6E86)
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkAccent = Color(0xFFFFB547)
val DarkOnAccent = Color(0xFF1A1203)
val DarkSuccess = Color(0xFF5EE6B0)
val DarkAlert = Color(0xFFFF6B6B)
val DarkTextPrimary = Color(0xFFEAF4F6)
val DarkTextSecondary = Color(0xFF9DB4BB)
val DarkTextDisabled = DarkTextPrimary.copy(alpha = 0.40f)

// --- Light theme ---
val LightBackground = Color(0xFFF4F8F9)
val LightSurface = Color(0xFFFFFFFF)
val LightPrimary = Color(0xFF1F6A85)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightAccent = Color(0xFFE59A1F)
val LightOnAccent = Color(0xFF1A1203)
val LightSuccess = DarkSuccess
val LightAlert = DarkAlert
val LightTextPrimary = Color(0xFF0E1E25)
val LightTextSecondary = Color(0xFF4A5F66)
val LightTextDisabled = LightTextPrimary.copy(alpha = 0.40f)

// --- Glass surfaces (dark-first "glass on gradient") ---
// Surface = white 8% alpha with a 1dp border of white 14% alpha.
val GlassSurfaceDark = Color.White.copy(alpha = 0.08f)
val GlassBorderDark = Color.White.copy(alpha = 0.14f)
val GlassSurfaceLight = LightSurface.copy(alpha = 0.70f)
val GlassBorderLight = Color.Black.copy(alpha = 0.08f)
