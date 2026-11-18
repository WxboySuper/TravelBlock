package com.travelblock.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val TravelBlockLightColors = lightColorScheme(
    primary = TerminalBlue,
    onPrimary = GateSurface,
    primaryContainer = SoftSky,
    onPrimaryContainer = DeepNavy,
    secondary = SkyBlue,
    onSecondary = GateSurface,
    secondaryContainer = ColorTokens.boardingBlueSoft,
    onSecondaryContainer = DeepNavy,
    tertiary = RunwayAmber,
    background = CloudWhite,
    onBackground = DeepNavy,
    surface = GateSurface,
    onSurface = DeepNavy,
    surfaceVariant = SoftSky,
    onSurfaceVariant = MutedText,
    outline = AluminumGray,
)

private val TravelBlockDarkColors = darkColorScheme(
    primary = SkyBlue,
    onPrimary = DeepNavy,
    primaryContainer = Color(0xFF173D5B),
    onPrimaryContainer = Color(0xFFD9EEFF),
    secondary = Color(0xFF9CCBFF),
    onSecondary = DeepNavy,
    tertiary = RunwayAmber,
    background = Color(0xFF0B1722),
    onBackground = Color(0xFFE7F0F7),
    surface = Color(0xFF122332),
    onSurface = Color(0xFFE7F0F7),
    surfaceVariant = Color(0xFF203749),
    onSurfaceVariant = Color(0xFFB5C7D6),
    outline = Color(0xFF607789),
)

private object ColorTokens {
    val boardingBlueSoft = androidx.compose.ui.graphics.Color(0xFFDDEFFF)
}

@Composable
fun TravelBlockTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = darkTheme ?: isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (useDarkTheme) TravelBlockDarkColors else TravelBlockLightColors,
        typography = TravelBlockTypography,
        content = content,
    )
}
