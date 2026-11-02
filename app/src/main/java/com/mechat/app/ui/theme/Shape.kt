package com.mechat.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// DESIGN TOKENS — shapes (RULE 7). Cards 28dp; fields/chips/bars pill.
val CardRadius = 28.dp
val PillRadius = 100.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(CardRadius),
    extraLarge = CircleShape,
)
