package de.thomba.andropicsort.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandOnPrimaryContainer,
    secondary = BrandSecondary,
    onSecondary = BrandOnSecondary,
    secondaryContainer = BrandSecondaryContainer,
    onSecondaryContainer = DeepCharcoal,
    background = WarmSand,
    onBackground = DeepCharcoal,
    surface = WarmSand,
    onSurface = DeepCharcoal,
    surfaceVariant = WarmSand,
    onSurfaceVariant = OnSurfaceMuted,
    outline = OutlineSoft,
)

@Composable
fun AndroidPicSortTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) AppColors else AppColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content,
    )
}

