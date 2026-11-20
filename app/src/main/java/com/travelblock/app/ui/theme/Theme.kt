package com.travelblock.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private fun daylightColors(p: TravelPalette) = lightColorScheme(
    primary = p.accent,
    onPrimary = p.paper,
    primaryContainer = p.softSky,
    onPrimaryContainer = p.ink,
    secondary = p.duskBlue,
    onSecondary = p.paper,
    secondaryContainer = p.sand,
    onSecondaryContainer = p.ink,
    tertiary = p.highlight,
    onTertiary = p.ink,
    background = p.cream,
    onBackground = p.ink,
    surface = p.paper,
    onSurface = p.ink,
    surfaceVariant = p.sand,
    onSurfaceVariant = p.warmGray,
    outline = p.sand,
)

private fun nightColors(p: TravelPalette) = darkColorScheme(
    primary = p.lamp,
    onPrimary = p.nightSky,
    primaryContainer = p.nightSurface,
    onPrimaryContainer = p.cream,
    secondary = p.skyMist,
    onSecondary = p.nightSky,
    tertiary = p.highlight,
    background = p.nightSky,
    onBackground = p.cream,
    surface = p.nightSurface,
    onSurface = p.cream,
    surfaceVariant = p.nightLine,
    onSurfaceVariant = p.sand,
    outline = p.nightLine,
)

private val SoftShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

@Composable
fun TravelBlockTheme(darkTheme: Boolean? = null, content: @Composable () -> Unit) {
    val useDarkTheme = darkTheme ?: isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (useDarkTheme) nightColors(ActivePalette.current) else daylightColors(ActivePalette.current),
        typography = TravelBlockTypography,
        shapes = SoftShapes,
        content = content,
    )
}
