package de.thomba.andropicsort.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandOnPrimaryContainer,
    secondary = BrandSecondary,
    secondaryContainer = BrandSecondaryContainer,
    surfaceVariant = SurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = BrandPrimaryContainer,
    onPrimary = BrandOnPrimaryContainer,
    secondary = BrandSecondaryContainer,
    onSecondary = BrandSecondary,
    surfaceVariant = Color(0xFF2B313A),
)

@Composable
fun AndroidPicSortTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

