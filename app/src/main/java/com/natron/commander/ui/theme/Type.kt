package com.natron.commander.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CommanderTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 72.sp,
        lineHeight = 72.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 48.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)
