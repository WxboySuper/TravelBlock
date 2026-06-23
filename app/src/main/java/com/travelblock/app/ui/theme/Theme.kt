package com.travelblock.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

private object ColorTokens {
    val boardingBlueSoft = androidx.compose.ui.graphics.Color(0xFFDDEFFF)
}

@Composable
fun TravelBlockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TravelBlockLightColors,
        typography = TravelBlockTypography,
        content = content,
    )
}
