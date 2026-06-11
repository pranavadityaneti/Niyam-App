package com.myniyam.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.myniyam.app.data.ThemePref

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

private val NiyamDarkColors = darkColorScheme(
    primary = PumpkinOrange,
    onPrimary = Color.White,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkCard,
    onSurface = DarkInk,
    surfaceVariant = DarkChipFill,
    onSurfaceVariant = DarkInkMuted,
    secondary = SaladGreen,
    onSecondary = BottleGreen,
    outline = DarkHairline,
    outlineVariant = DarkHairline
)

@Composable
fun NiyamTheme(content: @Composable () -> Unit) {
    val dark = when (ThemeState.pref) {
        ThemePref.LIGHT -> false
        ThemePref.DARK -> true
        ThemePref.SYSTEM -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(LocalNiyamColors provides niyamColorsFor(dark)) {
        MaterialTheme(
            colorScheme = if (dark) NiyamDarkColors else NiyamLightColors,
            typography = NiyamTypography,
            content = content
        )
    }
}
