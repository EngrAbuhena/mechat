package com.mechat.app.ui.theme

import androidx.compose.ui.unit.dp

// ============================================================
// DESIGN TOKENS — non-color values. The ONLY place sizes/alpha
// are defined (RULE 7). No magic numbers in screens/components.
// ============================================================

object Sizes {
    val touchTargetMin = 48.dp          // every touch target >= 48dp
    val fab = 60.dp                     // floating buttons 56–64dp
    val iconButton = 48.dp              // round icon buttons
    val avatar = 48.dp
    val pinDot = 14.dp
    val pinDotSpacing = 12.dp
    val borderThin = 1.dp
}

object Alpha {
    val backgroundPattern = 0.05f      // pattern at <= 5% opacity
    val disabled = 0.40f
}
