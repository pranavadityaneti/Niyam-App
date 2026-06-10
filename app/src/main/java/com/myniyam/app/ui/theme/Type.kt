package com.myniyam.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.myniyam.app.R

val PlayfairFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.SemiBold)
)

val InterFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium)
)

// Variable fonts: weight axes resolve on API 28+; API 26-27 render the
// default instance — accepted v1 limitation (spec §11).
val NiyamTypography = Typography(
    displayMedium = TextStyle(fontFamily = PlayfairFamily, fontWeight = FontWeight.SemiBold, fontSize = 40.sp),
    headlineMedium = TextStyle(fontFamily = PlayfairFamily, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.5.sp)
)
