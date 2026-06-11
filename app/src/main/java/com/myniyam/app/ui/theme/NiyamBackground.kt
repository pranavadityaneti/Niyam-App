package com.myniyam.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.myniyam.app.data.ThemePref

/**
 * Sunrise-gradient canvas (SP-8 spec §2). Vertical top→bottom brushes with the
 * founder-locked stops. [NiyamBackground] paints the canvas behind [content],
 * filling the window edge-to-edge (status bar included — MainActivity enables
 * edge-to-edge + transparent bars in Task 1.6). Theme-resolved via [ThemeState],
 * matching NiyamTheme's own LIGHT/DARK/SYSTEM logic.
 */
private val LightSunrise = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFD9B8),
        0.30f to Color(0xFFFFEDDC),
        0.60f to Color(0xFFF7EEE3),
        1.0f to Color(0xFFF5EBE1)
    )
)

/** Slightly warmer variant for the mantra overlay Compose surfaces (if any reuse it). */
val OverlaySunrise = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFE3C7),
        0.45f to Color(0xFFFAEEDF),
        1.0f to Color(0xFFF5EBE1)
    )
)

private val DarkSunrise = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFF17503A),
        0.35f to Color(0xFF0E382A),
        1.0f to Color(0xFF0A2A20)
    )
)

@Composable
fun NiyamBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dark = when (ThemeState.pref) {
        ThemePref.LIGHT -> false
        ThemePref.DARK -> true
        ThemePref.SYSTEM -> isSystemInDarkTheme()
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (dark) DarkSunrise else LightSunrise)
    ) {
        content()
    }
}
