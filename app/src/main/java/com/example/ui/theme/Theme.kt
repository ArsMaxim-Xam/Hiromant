package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MysticColorScheme = darkColorScheme(
    primary = MysticGold,
    onPrimary = Color.Black,
    secondary = MysticBronze,
    onSecondary = Color.White,
    tertiary = MysticSecondary,
    onTertiary = Color.White,
    background = MysticDarkBackground,
    onBackground = Color(0xFFE2E2EC),
    surface = MysticDarkSurface,
    onSurface = Color(0xFFE2E2EC),
    surfaceVariant = MysticDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC8C8DB),
    outline = MysticGold,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MysticColorScheme,
        typography = Typography,
        content = content
    )
}
