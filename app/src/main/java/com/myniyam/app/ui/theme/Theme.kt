package com.myniyam.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PlaceholderPrimary,
    secondary = PlaceholderSecondary,
    tertiary = PlaceholderTertiary
)

private val DarkColors = darkColorScheme(
    primary = PlaceholderPrimary,
    secondary = PlaceholderSecondary,
    tertiary = PlaceholderTertiary
)

@Composable
fun NiyamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = NiyamTypography,
        content = content
    )
}
