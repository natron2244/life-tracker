package com.natron.commander.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CommanderColorScheme = darkColorScheme(
    background       = CardBack,
    surface          = SurfaceVariant,
    onBackground     = OnSurface,
    onSurface        = OnSurface,
    primary          = MythicGold,
    onPrimary        = CardBack,
    secondary        = MythicGoldDim,
    onSecondary      = OnSurface,
    surfaceVariant   = Color(0xFF2A2620),
    onSurfaceVariant = OnSurfaceMuted,
    outline          = Color(0xFF4A4438),
    error            = WarningRed,
    onError          = OnSurface
)

@Composable
fun CommanderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CommanderColorScheme,
        typography  = CommanderTypography,
        content     = content
    )
}
