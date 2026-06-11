package com.myniyam.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.myniyam.app.R

/**
 * Outfit (Google Fonts, OFL) — the v2 typeface, replacing Playfair + Inter
 * everywhere (SP-8 spec §2). Bundled as a single variable TTF; each weight is
 * a FontVariation instance on the `wght` axis. The variable-axis Font(...)
 * overload + FontVariation.Settings apply on API 26+ (minSdk 26). Latin-only:
 * Indic mantra scripts fall back to the system Noto fonts automatically — this
 * is expected and correct (spec §2).
 */
@OptIn(ExperimentalTextApi::class)
private fun outfit(weight: FontWeight) = Font(
    R.font.outfit_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val OutfitFamily = FontFamily(
    outfit(FontWeight.Normal),    // 400 body
    outfit(FontWeight.Medium),    // 500 warm accents / overlines
    outfit(FontWeight.SemiBold),  // 600 card titles + buttons
    outfit(FontWeight.Bold)       // 700 hero numerals / wordmark
)

/**
 * Material3 type scale mapped to the spec §2 weight hierarchy:
 *   displayLarge/Medium → 700 hero numerals + wordmark, tight tracking (−0.5sp)
 *   headlineMedium      → 700 section/screen titles
 *   titleLarge          → 600 card titles
 *   bodyLarge/Medium    → 400 body
 *   labelLarge          → 600 button labels
 *   labelSmall          → 500 overlines (small caps applied at call sites via .uppercase())
 */
val NiyamTypography = Typography(
    displayLarge = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Bold, fontSize = 56.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.25).sp),
    titleLarge = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 2.sp)
)
