package com.mechat.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

// TODO: bundle Space Grotesk (SIL OFL) TTFs — Regular 400, Medium 500, Bold 700 —
// into app/src/main/res/font as space_grotesk_regular/medium/bold and load them here.
// Download was not possible in the build environment; default sans-serif is used for now.
// Never use downloadable fonts / Google Fonts provider (RULE 7).
private val MeChatFontFamily = FontFamily.Default

// Tabular numbers for IDs, PINs and timers.
val TabularTextStyle = TextStyle(fontFeatureSettings = "tnum")

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MeChatFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,   // display
        lineHeight = 40.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = MeChatFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,   // title
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = MeChatFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,   // body
        lineHeight = 24.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = MeChatFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,   // label
        lineHeight = 18.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = MeChatFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,   // caption
        lineHeight = 16.sp,
    ),
)
