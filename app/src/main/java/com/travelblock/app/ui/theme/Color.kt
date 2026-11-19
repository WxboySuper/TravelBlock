package com.travelblock.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * One complete colour direction for the app. The screens read the named colours below (Cream, Paper, Ink ...),
 * which resolve to whichever palette is active, so switching palette restyles everything at once.
 */
data class TravelPalette(
    val id: String,
    val label: String,
    val tagline: String,
    val cream: Color,
    val paper: Color,
    val sand: Color,
    val ink: Color,
    val duskBlue: Color,
    val softSky: Color,
    val skyMist: Color,
    val accent: Color,
    val highlight: Color,
    val sage: Color,
    val warmGray: Color,
    val lamp: Color,
    val nightSky: Color,
    val nightSurface: Color,
    val nightLine: Color,
)

private val Apron = TravelPalette(
    id = "apron", label = "Apron", tagline = "Cobalt on cool concrete with an aqua signal. Crisp, like a modern terminal.",
    cream = Color(0xFFEDF0F5), paper = Color(0xFFFFFFFF), sand = Color(0xFFD5DCE6), ink = Color(0xFF0E1A33),
    duskBlue = Color(0xFF1B2F8F), softSky = Color(0xFFE3EAFF), skyMist = Color(0xFF7C9CFF),
    accent = Color(0xFF2745F2), highlight = Color(0xFF14C3BA), sage = Color(0xFF0F9F78),
    warmGray = Color(0xFF55647C), lamp = Color(0xFF8FE4FF),
    nightSky = Color(0xFF0A1122), nightSurface = Color(0xFF121C36), nightLine = Color(0xFF26335A),
)

private val HarborDusk = TravelPalette(
    id = "harbor", label = "Harbor Dusk", tagline = "Deep blue ink, sky and sage. Calm like a study at dusk.",
    cream = Color(0xFFF1F5F9), paper = Color(0xFFFAFCFE), sand = Color(0xFFDCE4EC), ink = Color(0xFF1D2A44),
    duskBlue = Color(0xFF34527A), softSky = Color(0xFFE1EBF5), skyMist = Color(0xFF6F9BC4),
    accent = Color(0xFF2F5F91), highlight = Color(0xFF9CC3E6), sage = Color(0xFF5E9A77),
    warmGray = Color(0xFF66748A), lamp = Color(0xFFB4D8F5),
    nightSky = Color(0xFF111A2E), nightSurface = Color(0xFF1B2640), nightLine = Color(0xFF2F3E5E),
)

private val Lagoon = TravelPalette(
    id = "lagoon", label = "Lagoon", tagline = "Teal and slate with mint. Fresh, like a coastal terminal.",
    cream = Color(0xFFF0F6F5), paper = Color(0xFFF9FDFC), sand = Color(0xFFD7E5E2), ink = Color(0xFF1C3340),
    duskBlue = Color(0xFF2C5F6B), softSky = Color(0xFFDCEDEB), skyMist = Color(0xFF6FAAB0),
    accent = Color(0xFF1E7777), highlight = Color(0xFF9AD1C8), sage = Color(0xFF4E9A6B),
    warmGray = Color(0xFF627879), lamp = Color(0xFFB8F0E3),
    nightSky = Color(0xFF0F1D24), nightSurface = Color(0xFF182F38), nightLine = Color(0xFF2A4752),
)

private val TwilightPlum = TravelPalette(
    id = "plum", label = "Twilight Plum", tagline = "Muted plum and dusty rose. Quiet, like the last flight of the night.",
    cream = Color(0xFFF6F2F6), paper = Color(0xFFFCFAFC), sand = Color(0xFFE3DAE3), ink = Color(0xFF2D2240),
    duskBlue = Color(0xFF5A4A7C), softSky = Color(0xFFE8E2F0), skyMist = Color(0xFF9C8BC0),
    accent = Color(0xFF714680), highlight = Color(0xFFD7A9C6), sage = Color(0xFF6A9A86),
    warmGray = Color(0xFF76707F), lamp = Color(0xFFEFCDE2),
    nightSky = Color(0xFF181427), nightSurface = Color(0xFF261F3C), nightLine = Color(0xFF3B3258),
)

private val GoldenHour = TravelPalette(
    id = "golden", label = "Golden Hour", tagline = "The original warm paper, marigold and terracotta.",
    cream = Color(0xFFFAF4E8), paper = Color(0xFFFFFBF3), sand = Color(0xFFE8DFCF), ink = Color(0xFF25304A),
    duskBlue = Color(0xFF34527A), softSky = Color(0xFFE4ECF2), skyMist = Color(0xFF6F9BC4),
    accent = Color(0xFFCF6F4A), highlight = Color(0xFFE3A33B), sage = Color(0xFF5E9A77),
    warmGray = Color(0xFF7A7468), lamp = Color(0xFFF2C46D),
    nightSky = Color(0xFF141B2D), nightSurface = Color(0xFF1E2740), nightLine = Color(0xFF34405F),
)

/** The palettes offered in Settings. The first is the default. */
val TravelPalettes: List<TravelPalette> = listOf(Apron, HarborDusk, Lagoon, TwilightPlum, GoldenHour)

fun paletteById(id: String?): TravelPalette = TravelPalettes.firstOrNull { it.id == id } ?: TravelPalettes.first()

/** The palette currently in use. Reading it inside a composable makes that composable follow changes. */
object ActivePalette {
    var current: TravelPalette by mutableStateOf(TravelPalettes.first())
}

private val palette: TravelPalette get() = ActivePalette.current

// Core palette (resolved through the active palette)
val Cream: Color get() = palette.cream
val Paper: Color get() = palette.paper
val Sand: Color get() = palette.sand
val Ink: Color get() = palette.ink
val DuskBlue: Color get() = palette.duskBlue
val SoftSky: Color get() = palette.softSky
val SkyMist: Color get() = palette.skyMist
val Marigold: Color get() = palette.highlight
val Terracotta: Color get() = palette.accent
val Sage: Color get() = palette.sage
val WarmGray: Color get() = palette.warmGray

// Night flight (cockpit / dark theme)
val NightSky: Color get() = palette.nightSky
val NightSurface: Color get() = palette.nightSurface
val NightLine: Color get() = palette.nightLine
val Lamp: Color get() = palette.lamp

// Legacy names kept so existing screens pick up the active palette.
val SkyBlue: Color get() = SkyMist
val TerminalBlue: Color get() = DuskBlue
val DeepNavy: Color get() = Ink
val RunwayAmber: Color get() = Marigold
val AluminumGray: Color get() = Sand
val TicketWhite: Color get() = Paper
val SuccessGreen: Color get() = Sage
val DepartureBlue: Color get() = DuskBlue
val BoardingBlue: Color get() = SkyMist
val CabinAmber: Color get() = Marigold
val CloudWhite: Color get() = Cream
val GateSurface: Color get() = Paper
val SlateText: Color get() = Ink
val MutedText: Color get() = WarmGray
val RunwayLine: Color get() = Sand
