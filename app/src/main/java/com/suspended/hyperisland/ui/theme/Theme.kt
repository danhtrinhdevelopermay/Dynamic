package com.suspended.hyperisland.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IslandColors.Primary,
    secondary = IslandColors.AccentBlue,
    tertiary = IslandColors.AccentPurple,
    background = IslandColors.BackgroundDark,
    surface = IslandColors.Surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = IslandColors.TextPrimary,
    onSurface = IslandColors.TextPrimary
)

@Composable
fun HyperIslandTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
