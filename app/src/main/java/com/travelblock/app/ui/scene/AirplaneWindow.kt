package com.travelblock.app.ui.scene

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * The view from a window seat: an oval-ish cabin window with a plastic bezel looking out at the sky for the
 * given local hour, the wing with its blinking light, and a pull-down shade. Tiny turbulence sway keeps it alive.
 */
@Composable
fun AirplaneWindow(localHour: Float, modifier: Modifier = Modifier) {
    val outer = RoundedCornerShape(140.dp)
    val inner = RoundedCornerShape(116.dp)
    val transition = rememberInfiniteTransition(label = "window")
    val sway by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sway")

    Box(
        modifier
            .clip(outer)
            .background(Brush.verticalGradient(listOf(Color(0xFFE7E1D6), Color(0xFFB9B2A6))))
            .padding(10.dp),
    ) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(126.dp)).background(Color(0xFF8E887D)).padding(5.dp)) {
            Box(Modifier.fillMaxSize().clip(inner).background(Color.Black)) {
                Box(Modifier.fillMaxSize().graphicsLayer { translationY = sway * 5f; rotationZ = sway * 0.35f; scaleX = 1.04f; scaleY = 1.04f }) {
                    SkyScene(localHour)
                    Wing()
                }
                Glass()
                Shade()
            }
        }
    }
}

@Composable
private fun Wing() {
    val blink = rememberInfiniteTransition(label = "wing")
    val light by blink.animateFloat(0.15f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "light")
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val wing = Path().apply {
            moveTo(w * 1.05f, h * 0.80f)
            lineTo(w * 0.40f, h * 0.915f)
            lineTo(w * 0.36f, h * 0.955f)
            lineTo(w * 1.05f, h * 1.04f)
            close()
        }
        drawPath(wing, Brush.linearGradient(listOf(Color(0xFFF4F1EA), Color(0xFFB6B7BC), Color(0xFF7D8088)), Offset(w, h * 0.8f), Offset(w * 0.3f, h * 1.0f)))
        // Panel lines and engine glint.
        drawLine(Color(0x33000000), Offset(w * 0.95f, h * 0.84f), Offset(w * 0.45f, h * 0.925f), strokeWidth = 2f)
        drawLine(Color(0x22000000), Offset(w * 0.98f, h * 0.90f), Offset(w * 0.42f, h * 0.945f), strokeWidth = 2f)
        // Winglet.
        val winglet = Path().apply {
            moveTo(w * 0.40f, h * 0.915f)
            lineTo(w * 0.355f, h * 0.775f)
            lineTo(w * 0.425f, h * 0.775f)
            lineTo(w * 0.47f, h * 0.905f)
            close()
        }
        drawPath(winglet, Brush.horizontalGradient(listOf(Color(0xFFD7D9DE), Color(0xFF9A9DA6)), startX = w * 0.35f, endX = w * 0.47f))
        // Blinking nav light and its glow.
        val tip = Offset(w * 0.388f, h * 0.775f)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFF4B4B).copy(alpha = 0.55f * light), Color.Transparent), tip, 90f), 90f, tip)
        drawCircle(Color(0xFFFF6A6A).copy(alpha = light), 9f, tip)
    }
}

/** Soft reflection and edge vignette, so it reads as thick glass rather than a flat picture. */
@Composable
private fun Glass() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.linearGradient(listOf(Color(0x26FFFFFF), Color.Transparent, Color(0x10FFFFFF)), Offset.Zero, Offset(size.width, size.height * 0.6f)))
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color(0x55000000)), center = center, radius = size.maxDimension * 0.75f))
    }
}

/** A partly pulled-down plastic shade with ridges and a handle notch. */
@Composable
private fun Shade() {
    Canvas(Modifier.fillMaxSize()) {
        val shadeHeight = size.height * 0.1f
        drawRect(Brush.verticalGradient(listOf(Color(0xFFE9E3D8), Color(0xFFCFC8BB))), size = Size(size.width, shadeHeight))
        for (i in 1..3) drawLine(Color(0x22000000), Offset(0f, shadeHeight * i / 4f), Offset(size.width, shadeHeight * i / 4f), strokeWidth = 2f)
        drawRect(Color(0x55000000), topLeft = Offset(0f, shadeHeight), size = Size(size.width, 6f))
        drawRoundRect(Color(0xFFB7B0A3), Offset(size.width / 2 - 46f, shadeHeight - 14f), Size(92f, 22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(11f, 11f))
    }
}
