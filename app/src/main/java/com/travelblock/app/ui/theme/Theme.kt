package com.travelblock.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val DaylightColors = lightColorScheme(
    primary = DuskBlue,
    onPrimary = Paper,
    primaryContainer = SoftSky,
    onPrimaryContainer = Ink,
    secondary = Terracotta,
    onSecondary = Paper,
    secondaryContainer = Sand,
    onSecondaryContainer = Ink,
    tertiary = Marigold,
    onTertiary = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = WarmGray,
    outline = Sand,
)

private val NightColors = darkColorScheme(
    primary = Lamp,
    onPrimary = NightSky,
    primaryContainer = NightSurface,
    onPrimaryContainer = Cream,
    secondary = SkyMist,
    onSecondary = NightSky,
    tertiary = Marigold,
    background = NightSky,
    onBackground = Cream,
    surface = NightSurface,
    onSurface = Cream,
    surfaceVariant = NightLine,
    onSurfaceVariant = Sand,
    outline = NightLine,
)

private val SoftShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun TravelBlockTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) NightColors else DaylightColors,
        typography = TravelBlockTypography,
        shapes = SoftShapes,
        content = content,
    )
}
