package com.myniyam.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Brand extension tokens that are NOT part of the M3 [androidx.compose.material3.ColorScheme]
 * but are consumed directly by Compose screens (chips, selection fills, info-chip text).
 *
 * Resolved theme-aware via [LocalNiyamColors], provided by [NiyamTheme]. Light values are
 * identical to the original hardcoded brand tokens, so light mode is pixel-unchanged; dark
 * mode swaps in the SP-7 dark equivalents.
 */
data class NiyamExtraColors(
    /** Warm orange selection fill (segments, filter chips, selectable cards). */
    val orangeTint: Color,
    /** Label/icon color drawn on top of [orangeTint] (e.g. FilterChip selected label). */
    val onTint: Color,
    /** Neutral chip background (info chips). */
    val chipFill: Color,
    /** Muted ink for secondary text on chips. */
    val inkMuted: Color,
    /** Small-caps overline accent (warm brown light / soft amber dark). */
    val overlineWarm: Color
)

private val LightNiyamColors = NiyamExtraColors(
    orangeTint = OrangeTint,
    onTint = BottleGreen,
    chipFill = ChipFill,
    inkMuted = InkMuted,
    overlineWarm = OverlineWarm
)

private val DarkNiyamColors = NiyamExtraColors(
    orangeTint = DarkOrangeTint,
    onTint = DarkInk,
    chipFill = DarkChipFill,
    inkMuted = DarkInkMuted,
    overlineWarm = DarkOverlineWarm
)

internal fun niyamColorsFor(dark: Boolean): NiyamExtraColors =
    if (dark) DarkNiyamColors else LightNiyamColors

val LocalNiyamColors = staticCompositionLocalOf { LightNiyamColors }

/** Accessor mirroring `MaterialTheme.colorScheme`: `NiyamTheme.colors.orangeTint`, etc. */
object NiyamTheme {
    val colors: NiyamExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNiyamColors.current
}
