package com.travelblock.app.ui.scene

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.theme.Lamp
import com.travelblock.app.ui.theme.Terracotta

/** The check-in desk: a sign, the luggage scale and your bag waiting on the belt. */
@Composable
fun CheckInDeskScene(flightNumber: String, gate: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(184.dp).clip(RoundedCornerShape(26.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(Brush.verticalGradient(listOf(Color(0xFFF6EBD6), Color(0xFFE6D5B8))))
            // Ceiling lights.
            for (i in 0..3) drawRoundRect(Color(0xFFFFF7E0), Offset(w * (0.08f + i * 0.24f), 10f), Size(w * 0.14f, 8f), CornerRadius(4f))
            // Back wall panel with a sign.
            drawRoundRect(Color(0xFF22212A), Offset(w * 0.1f, h * 0.08f), Size(w * 0.8f, h * 0.24f), CornerRadius(14f))
            // Counter.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8A5B3A), Color(0xFF633F26))), Offset(0f, h * 0.62f), Size(w, h * 0.38f))
            drawRect(Color(0xFFB98557), Offset(0f, h * 0.60f), Size(w, h * 0.045f))
            // Luggage scale platform and display.
            drawRoundRect(Color(0xFF3A3944), Offset(w * 0.52f, h * 0.55f), Size(w * 0.32f, h * 0.07f), CornerRadius(6f))
            drawRoundRect(Color(0xFF1A1A20), Offset(w * 0.74f, h * 0.45f), Size(w * 0.12f, h * 0.1f), CornerRadius(5f))
            drawRect(Lamp, Offset(w * 0.76f, h * 0.49f), Size(w * 0.08f, 5f))
            // Suitcase on the scale.
            drawRoundRect(Terracotta, Offset(w * 0.58f, h * 0.38f), Size(w * 0.14f, h * 0.20f), CornerRadius(14f))
            drawRoundRect(Color(0x33000000), Offset(w * 0.58f, h * 0.46f), Size(w * 0.14f, 4f), CornerRadius(2f))
            drawRoundRect(Color(0xFF633F26), Offset(w * 0.625f, h * 0.32f), Size(w * 0.05f, h * 0.07f), CornerRadius(5f), style = androidx.compose.ui.graphics.drawscope.Stroke(5f))
            // A luggage tag hanging off the handle.
            drawLine(Color(0xFFFFE08A), Offset(w * 0.69f, h * 0.40f), Offset(w * 0.71f, h * 0.47f), strokeWidth = 3f)
            drawRoundRect(Color(0xFFFFF7E0), Offset(w * 0.685f, h * 0.46f), Size(w * 0.06f, h * 0.09f), CornerRadius(5f))
            // Belt rollers.
            for (i in 0..10) drawCircle(Color(0xFF4A4955), 5f, Offset(w * 0.04f + i * w * 0.045f, h * 0.7f))
        }
        Text(
            "CHECK-IN  /  $flightNumber  /  GATE $gate",
            style = MaterialTheme.typography.labelLarge,
            color = Lamp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp),
        )
    }
}

/** Walking down the jet bridge: lights stream toward you and the plane's door waits at the end. */
@Composable
fun JetBridgeScene(modifier: Modifier = Modifier, localHour: Float = 14f) {
    val transition = rememberInfiniteTransition(label = "bridge")
    val walk by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "walk")
    Box(modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(26.dp))) {
        SkyScene(localHour, Modifier.fillMaxSize(), speed = 0.4f)
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val vx = w / 2
            val vy = h * 0.46f
            val doorW = w * 0.2f
            val doorH = h * 0.34f
            // Corridor walls, ceiling and floor converge on the open door.
            val left = Path().apply { moveTo(0f, 0f); lineTo(vx - doorW / 2, vy - doorH / 2); lineTo(vx - doorW / 2, vy + doorH / 2); lineTo(0f, h); close() }
            val right = Path().apply { moveTo(w, 0f); lineTo(vx + doorW / 2, vy - doorH / 2); lineTo(vx + doorW / 2, vy + doorH / 2); lineTo(w, h); close() }
            val ceiling = Path().apply { moveTo(0f, 0f); lineTo(w, 0f); lineTo(vx + doorW / 2, vy - doorH / 2); lineTo(vx - doorW / 2, vy - doorH / 2); close() }
            val floor = Path().apply { moveTo(0f, h); lineTo(w, h); lineTo(vx + doorW / 2, vy + doorH / 2); lineTo(vx - doorW / 2, vy + doorH / 2); close() }
            drawPath(left, Brush.horizontalGradient(listOf(Color(0xFF8F95A3), Color(0xFFC3C7D0)), 0f, vx))
            drawPath(right, Brush.horizontalGradient(listOf(Color(0xFFC3C7D0), Color(0xFF8F95A3)), vx, w))
            drawPath(ceiling, Brush.verticalGradient(listOf(Color(0xFFE9E6E0), Color(0xFFBDBCB8)), 0f, vy))
            drawPath(floor, Brush.verticalGradient(listOf(Color(0xFF6C4E49), Color(0xFF8A6A5F)), vy, h))
            // Streaming lights and floor stripes, spaced in perspective.
            for (i in 0..5) {
                val t = ((i + walk) / 6f)
                val e = t * t
                val ly = vy - doorH / 2 - (vy - doorH / 2) * e
                val lw = doorW * 0.5f + (w * 0.5f) * e
                drawRoundRect(Color(0xFFFFF6D6), Offset(vx - lw / 2, ly), Size(lw, 6f + 14f * e), CornerRadius(6f))
                val fy = vy + doorH / 2 + (h - vy - doorH / 2) * e
                drawLine(Color(0x55FFFFFF), Offset(vx - doorW / 2 - (vx - doorW / 2) * e, fy), Offset(vx + doorW / 2 + (vx - doorW / 2) * e, fy), strokeWidth = 2f + 3f * e)
            }
            // The aircraft doorway glowing at the end.
            drawRoundRect(Color(0xFFFFF1CF), Offset(vx - doorW / 2, vy - doorH / 2), Size(doorW, doorH), CornerRadius(14f))
            drawRoundRect(Color(0xFFCF6F4A), Offset(vx - doorW / 2 - 5f, vy - doorH / 2 - 5f), Size(doorW + 10f, doorH + 10f), CornerRadius(18f), style = androidx.compose.ui.graphics.drawscope.Stroke(5f))
        }
    }
}

private fun Modifier.fillMaxSize(): Modifier = this.fillMaxWidth().height(2000.dp)

/**
 * Arriving in a city: its skyline against the sky at that city's real local time, windows lit after
 * dark, and your plane drifting down toward the runway.
 */
@Composable
fun ArrivalScene(cityCode: String, longitude: Double, modifier: Modifier = Modifier) {
    val hour = androidx.compose.runtime.remember(cityCode) { localSolarHour(java.time.Instant.now(), longitude) }
    val night = skyPaletteFor(hour).night
    val transition = rememberInfiniteTransition(label = "arrival")
    val glide by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart), label = "glide")
    Box(modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(26.dp))) {
        SkyScene(hour, Modifier.fillMaxSize(), speed = 0.35f)
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Landing plane gliding in from the left.
            val px = w * (-0.1f + 1.2f * glide)
            val py = h * (0.18f + 0.22f * glide)
            val body = Color(0xFFF4F1EA)
            drawRoundRect(body, Offset(px, py), Size(w * 0.13f, h * 0.045f), CornerRadius(14f))
            drawRect(Color(0xFFCF6F4A), Offset(px + w * 0.015f, py - h * 0.04f), Size(w * 0.02f, h * 0.045f))
            drawLine(Color(0xFFB5B2AC), Offset(px + w * 0.05f, py + h * 0.02f), Offset(px + w * 0.09f, py + h * 0.07f), strokeWidth = 6f)
            val base = h * 0.84f
            drawArrivalSkyline(cityCode, night)
            drawRect(Color(0xFF1B1D2A), Offset(0f, base), Size(w, h - base))
            drawRect(Color(0xFFFFE08A), Offset(0f, base + 8f), Size(w, 3f))
        }
    }
}

private data class SkylineBuilding(val x: Float, val base: Float, val width: Float, val height: Float)

private fun DrawScope.drawArrivalSkyline(cityCode: String, night: Float) {
    val seed = cityCode.fold(7) { acc, c -> acc * 31 + c.code }
    var x = -10f
    var index = 0
    while (x < size.width) {
        val height = buildingHeight(seed, index, size.height)
        val width = buildingWidth(seed, index, size.width)
        val building = SkylineBuilding(x, size.height * 0.84f, width, height)
        drawBuilding(building, seed, index, night)
        x += width + 3f
        index++
    }
}

private fun buildingWidth(seed: Int, index: Int, canvasWidth: Float): Float {
    val variation = ((seed + index * 7919) % 100 + 100) % 100 / 100f
    return canvasWidth * (0.05f + 0.06f * variation)
}

private fun buildingHeight(seed: Int, index: Int, canvasHeight: Float): Float {
    val variation = (((seed / 3 + index * 104729) % 100 + 100) % 100) / 100f
    return canvasHeight * (0.12f + 0.30f * variation)
}

private fun DrawScope.drawBuilding(building: SkylineBuilding, seed: Int, index: Int, night: Float) {
    val variation = ((seed + index * 7919) % 100 + 100) % 100 / 100f
    val shade = Color(0xFF1F2336).copy(alpha = 0.55f + 0.4f * variation)
    drawRect(shade, Offset(building.x, building.base - building.height), Size(building.width, building.height))
    if (shouldDrawWindows(night, index)) drawWindows(building, index)
}

private fun shouldDrawWindows(night: Float, index: Int): Boolean =
    night > 0.35f || isAlternatingWindowBand(night, index)

private fun isAlternatingWindowBand(night: Float, index: Int): Boolean = night > 0.2f && index % 2 == 0

private fun DrawScope.drawWindows(building: SkylineBuilding, index: Int) {
    var windowY = building.base - building.height + 12f
    while (windowY < building.base - 14f) {
        drawWindowRow(building, windowY, index)
        windowY += 20f
    }
}

private fun DrawScope.drawWindowRow(building: SkylineBuilding, windowY: Float, index: Int) {
    var windowX = building.x + 8f
    while (windowX < building.x + building.width - 10f) {
        if ((windowX.toInt() + windowY.toInt() + index) % 3 != 0) {
            drawRect(Color(0xFFFFD98A), Offset(windowX, windowY), Size(6f, 8f))
        }
        windowX += 16f
    }
}
