package com.travelblock.app.ui.scene

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import java.time.Instant
import java.time.ZoneOffset
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/** Colours of the sky at a moment, from zenith to horizon. */
data class SkyPalette(
    val top: Color,
    val middle: Color,
    val horizon: Color,
    val cloud: Color,
    /** 0 = full day, 1 = deep night. Drives stars and cabin dimming. */
    val night: Float,
)

private data class SkyKey(val hour: Float, val palette: SkyPalette)

private val keys = listOf(
    SkyKey(0f, SkyPalette(Color(0xFF070B1C), Color(0xFF0E1630), Color(0xFF1B2347), Color(0xFF2A3358), 1f)),
    SkyKey(5f, SkyPalette(Color(0xFF0B1230), Color(0xFF1A2650), Color(0xFF3A3A6B), Color(0xFF39446F), 0.9f)),
    SkyKey(6.2f, SkyPalette(Color(0xFF2E3F7A), Color(0xFFB9748F), Color(0xFFFFB778), Color(0xFFFFD9B0), 0.45f)),
    SkyKey(8f, SkyPalette(Color(0xFF4F86C9), Color(0xFF8DBDE8), Color(0xFFD7E8F2), Color(0xFFFFFFFF), 0f)),
    SkyKey(12f, SkyPalette(Color(0xFF2F78C4), Color(0xFF6FB2E6), Color(0xFFBFE0F4), Color(0xFFFFFFFF), 0f)),
    SkyKey(16.5f, SkyPalette(Color(0xFF3F79BF), Color(0xFF86B4DE), Color(0xFFF2DDBE), Color(0xFFFFF4E2), 0f)),
    SkyKey(18.4f, SkyPalette(Color(0xFF3B4C93), Color(0xFFD5826A), Color(0xFFFFB55F), Color(0xFFFFCB94), 0.3f)),
    SkyKey(19.8f, SkyPalette(Color(0xFF1E2A5E), Color(0xFF5A4378), Color(0xFFE2806B), Color(0xFFB88CA0), 0.65f)),
    SkyKey(21.5f, SkyPalette(Color(0xFF0A1030), Color(0xFF18204A), Color(0xFF2C2F5C), Color(0xFF353F68), 0.95f)),
    SkyKey(24f, SkyPalette(Color(0xFF070B1C), Color(0xFF0E1630), Color(0xFF1B2347), Color(0xFF2A3358), 1f)),
)

/** Sky colours for a local solar hour (0..24). Blends smoothly between the keyframes. */
fun skyPaletteFor(localHour: Float): SkyPalette {
    val h = ((localHour % 24f) + 24f) % 24f
    val next = keys.indexOfFirst { it.hour >= h }.coerceAtLeast(1)
    val a = keys[next - 1]
    val b = keys[next]
    val t = ((h - a.hour) / (b.hour - a.hour)).coerceIn(0f, 1f)
    return SkyPalette(
        lerp(a.palette.top, b.palette.top, t),
        lerp(a.palette.middle, b.palette.middle, t),
        lerp(a.palette.horizon, b.palette.horizon, t),
        lerp(a.palette.cloud, b.palette.cloud, t),
        a.palette.night + (b.palette.night - a.palette.night) * t,
    )
}

/** Local mean solar time (hours) at a longitude for an instant, so the sky matches where the plane really is. */
fun localSolarHour(now: Instant, longitude: Double): Float {
    val utc = now.atOffset(ZoneOffset.UTC)
    val utcHour = utc.hour + utc.minute / 60f + utc.second / 3600f
    return (((utcHour + (longitude / 15.0).toFloat()) % 24f) + 24f) % 24f
}

private data class Cloud(val x: Float, val y: Float, val scale: Float, val layer: Int)

private val clouds = listOf(
    Cloud(0.05f, 0.34f, 1.1f, 0), Cloud(0.55f, 0.22f, 0.8f, 0), Cloud(0.85f, 0.46f, 1.3f, 0),
    Cloud(0.20f, 0.58f, 1.5f, 1), Cloud(0.70f, 0.66f, 1.8f, 1), Cloud(0.42f, 0.78f, 2.1f, 2), Cloud(0.95f, 0.84f, 2.4f, 2),
)

private val stars = List(70) { i ->
    val a = (i * 7919 % 1000) / 1000f
    val b = (i * 104729 % 1000) / 1000f
    Triple(a, b * 0.7f, 0.5f + (i % 5) * 0.25f)
}

/**
 * A moving sky: gradient for the local time, sun or moon arc, stars at night, and clouds drifting past
 * in three parallax layers so the plane feels like it is genuinely travelling.
 */
@Composable
fun SkyScene(localHour: Float, modifier: Modifier = Modifier.fillMaxSize(), speed: Float = 1f) {
    val palette = skyPaletteFor(localHour)
    val transition = rememberInfiniteTransition(label = "sky")
    val drift by transition.animateFloat(0f, 1f, infiniteRepeatable(tween((60_000 / speed).toInt(), easing = LinearEasing), RepeatMode.Restart), label = "drift")
    val twinkle by transition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "twinkle")

    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(palette.top, palette.middle, palette.horizon)))
        if (palette.night > 0.35f) drawStars(palette.night, twinkle)
        drawCelestial(localHour, palette)
        clouds.forEach { drawCloud(it, drift, palette) }
    }
}

private fun DrawScope.drawStars(night: Float, twinkle: Float) {
    val alpha = ((night - 0.35f) / 0.65f).coerceIn(0f, 1f)
    stars.forEach { (x, y, size) ->
        drawCircle(Color.White.copy(alpha = alpha * (0.35f + 0.65f * ((x * 7 + twinkle) % 1f))), radius = size * 2.2f, center = Offset(x * this.size.width, y * this.size.height))
    }
}

private fun DrawScope.drawCelestial(hour: Float, palette: SkyPalette) {
    val daytime = hour in 6f..18f
    val progress = if (daytime) (hour - 6f) / 12f else ((hour + 6f) % 24f) / 12f
    val x = size.width * (0.12f + 0.76f * progress)
    val y = size.height * (0.62f - 0.5f * sin((progress * PI).toFloat()))
    if (daytime) {
        val warm = if (palette.night > 0.2f) Color(0xFFFFC27A) else Color(0xFFFFF3C4)
        drawCircle(Brush.radialGradient(listOf(warm.copy(alpha = 0.55f), Color.Transparent), center = Offset(x, y), radius = size.width * 0.45f), radius = size.width * 0.45f, center = Offset(x, y))
        drawCircle(warm, radius = size.width * 0.07f, center = Offset(x, y))
    } else {
        drawCircle(Brush.radialGradient(listOf(Color(0x55DDE6FF), Color.Transparent), center = Offset(x, y), radius = size.width * 0.3f), radius = size.width * 0.3f, center = Offset(x, y))
        drawCircle(Color(0xFFEFF2FF), radius = size.width * 0.05f, center = Offset(x, y))
        drawCircle(palette.top, radius = size.width * 0.043f, center = Offset(x + size.width * 0.02f, y - size.width * 0.01f))
    }
}

private fun DrawScope.drawCloud(cloud: Cloud, drift: Float, palette: SkyPalette) {
    val speedFactor = 1f + cloud.layer * 1.6f
    val span = size.width * 1.6f
    val cx = ((cloud.x * span - drift * span * speedFactor) % span + span) % span - size.width * 0.3f
    val cy = size.height * cloud.y
    val r = size.width * 0.07f * cloud.scale
    val alpha = (0.6f + cloud.layer * 0.15f).coerceAtMost(0.95f)
    val lobes = listOf(-1.4f to 0.22f, -0.6f to -0.12f, 0.2f to -0.3f, 1.0f to -0.05f, 1.7f to 0.2f, 0.3f to 0.2f)
    // Draw the whole cloud opaque into its own layer, then composite once so overlaps do not show through.
    val bounds = androidx.compose.ui.geometry.Rect(cx - 3f * r, cy - 2f * r, cx + 3.2f * r, cy + 2f * r)
    val layer = androidx.compose.ui.graphics.Paint().apply { this.alpha = alpha }
    drawContext.canvas.saveLayer(bounds, layer)
    val shade = lerp(palette.cloud, palette.horizon, 0.55f)
    lobes.forEach { (dx, dy) -> drawCircle(shade, radius = r * 0.95f, center = Offset(cx + dx * r, cy + (dy + 0.22f) * r)) }
    lobes.forEach { (dx, dy) -> drawCircle(palette.cloud.copy(alpha = 1f), radius = r * (0.9f + 0.25f * kotlin.math.abs(dy)), center = Offset(cx + dx * r, cy + dy * r)) }
    drawContext.canvas.restore()
}

/** How dark the cabin should be, 0..1, derived from the same sky. */
fun cabinDimFor(localHour: Float): Float = max(0.0f, skyPaletteFor(localHour).night)
