package com.travelblock.app.ui.scene

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelblock.app.ui.theme.Lamp

/** The airport seen through the terminal's big windows: live sky, tarmac and a parked plane. */
@Composable
fun TerminalWindows(localHour: Float, modifier: Modifier = Modifier) {
    val night = skyPaletteFor(localHour).night
    Box(modifier.clipToBounds()) {
        SkyScene(localHour, Modifier.fillMaxSize(), speed = 0.5f)
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val ground = h * 0.74f
            // Tarmac with a faint reflection of the sky and runway dashes.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF3A3D47), Color(0xFF22242C)), startY = ground, endY = h), Offset(0f, ground), Size(w, h - ground))
            for (i in 0..8) drawRect(Color.White.copy(alpha = 0.35f - night * 0.15f), Offset(w * (0.04f + i * 0.12f), ground + (h - ground) * 0.55f), Size(w * 0.06f, 5f))
            drawParkedPlane(w, h, ground, night)
            // Window mullions and the heavy lower sill.
            val mullion = Color(0xFF2A2630).copy(alpha = 0.92f)
            listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f).forEach { x -> drawRect(mullion, Offset(w * x - 9f, 0f), Size(18f, h)) }
            drawRect(mullion, Offset(0f, h * 0.30f), Size(w, 12f))
            drawRect(Brush.verticalGradient(listOf(Color(0xFF2A2630), Color(0xFF16141B))), Offset(0f, h - 22f), Size(w, 22f))
            // Warm interior glow on the glass.
            drawRect(Brush.verticalGradient(listOf(Color(0x33FFE2B0), Color.Transparent)), size = Size(w, h * 0.5f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawParkedPlane(w: Float, h: Float, ground: Float, night: Float) {
    val fade = 1f - night * 0.45f
    val style = AirlinerStyle(
        body = Color(0xFFEDEAE4).copy(alpha = fade),
        shade = Color(0xFF9C9AA3).copy(alpha = fade),
        accent = Color(0xFFCF6F4A).copy(alpha = fade),
        window = Color(0xFF2A2630).copy(alpha = 0.85f),
    )
    val length = w * 0.62f
    val center = Offset(w * 0.6f, ground - length * 0.125f)
    drawAirliner(center, length, style, AirlinerPose(gearDown = true))
    // Jet bridge reaching the nose.
    drawRect(Color(0xFF8C8F98).copy(alpha = 0.95f), Offset(center.x - length * 0.5f - w * 0.07f, center.y - length * 0.06f), Size(w * 0.08f, length * 0.07f))
}

/** One airport-board character that flips down to the next one when its value changes. */
@Composable
fun FlapChar(char: Char, modifier: Modifier = Modifier, width: Dp = 26.dp, height: Dp = 38.dp, color: Color = Lamp, fontSize: Int = 22) {
    Box(
        modifier.size(width, height).background(Color(0xFF2B2A31), RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = char,
            transitionSpec = { (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut()) },
            label = "flap",
        ) { value ->
            Text(value.toString(), color = color, style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = fontSize.sp))
        }
        Box(Modifier.fillMaxSize().height(1.dp).background(Color.Transparent))
        Canvas(Modifier.fillMaxSize()) { drawLine(Color(0xFF14131A), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2f) }
    }
}

/** A word or number written in flap characters. */
@Composable
fun FlapText(text: String, modifier: Modifier = Modifier, length: Int = text.length, color: Color = Lamp, width: Dp = 26.dp, height: Dp = 38.dp, fontSize: Int = 22) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        text.uppercase().padEnd(length).take(length).forEach { FlapChar(it, color = color, width = width, height = height, fontSize = fontSize) }
    }
}
