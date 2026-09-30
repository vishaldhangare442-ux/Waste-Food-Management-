package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = DarkGreenOnPrimary,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = DarkGreenOnContainer,
    secondary = DarkGreenSecondary,
    onSecondary = Color.Black,
    tertiary = AmberTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = LeafGreenSecondary,
    onSecondary = LeafGreenOnSecondary,
    secondaryContainer = LeafGreenContainer,
    onSecondaryContainer = LeafGreenOnContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberOnContainer,
    background = CleanBackgroundLight,
    surface = CleanSurfaceLight,
    surfaceVariant = CleanSurfaceVariantLight,
    onBackground = CleanOnSurfaceLight,
    onSurface = CleanOnSurfaceLight
)

@Composable
fun FoodWasteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
