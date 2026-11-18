package com.travelblock.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.travelblock.app.R

@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: Int, soft: Boolean = false): Font {
    val settings = if (soft) {
        FontVariation.Settings(FontVariation.weight(weight), FontVariation.Setting("SOFT", 100f))
    } else {
        FontVariation.Settings(FontVariation.weight(weight))
    }
    return Font(resId, FontWeight(weight), variationSettings = settings)
}

/** Warm, bookish serif for headings and big numbers. */
val DisplayFamily = FontFamily(variable(R.font.fraunces, 500, soft = true), variable(R.font.fraunces, 700, soft = true))

/** Rounded, friendly sans for everything else. */
val BodyFamily = FontFamily(
    variable(R.font.nunito, 400), variable(R.font.nunito, 600), variable(R.font.nunito, 700), variable(R.font.nunito, 800),
)

val TravelBlockTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 54.sp),
    displayMedium = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 44.sp),
    displaySmall = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, letterSpacing = 0.02.em),
    labelMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.04.em),
    labelSmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.06.em),
)
