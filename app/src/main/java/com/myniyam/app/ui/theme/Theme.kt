package com.myniyam.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NiyamLightColors = lightColorScheme(
    primary = PumpkinOrange,
    onPrimary = Color.White,
    background = Eggshell,
    onBackground = BottleGreen,
    surface = CardWarm,
    onSurface = BottleGreen,
    surfaceVariant = ChipFill,
    onSurfaceVariant = InkMuted,
    secondary = SaladGreen,
    onSecondary = BottleGreen,
    outline = Hairline,
    outlineVariant = Hairline
)

@Composable
fun NiyamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NiyamLightColors,
        typography = NiyamTypography,
        content = content
    )
}
