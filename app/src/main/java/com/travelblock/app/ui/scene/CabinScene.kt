package com.travelblock.app.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import java.time.Instant

/** Where the camera looks: the aisle's vanishing point and the half-width of the cabin at the camera. */
private data class CabinView(val vanishX: Float, val vanishY: Float, val halfWidth: Float, val floorY: Float)

/** One row of seats at a given depth (0 = at the camera, 1 = at the far end). */
private data class CabinRow(val depth: Float, val view: CabinView)

private val CabinWall = Color(0xFFE9E2D4)
private val CabinBin = Color(0xFFF5F0E6)
private val CabinCarpet = Color(0xFF5B6B8C)
private val SeatCloth = Color(0xFF34527A)
private val SeatHeadrest = Color(0xFFF1E9DA)

/**
 * The view down the aisle of the cabin you are about to sit in: overhead bins, windows with the real sky
 * for the departure airport's local time, and rows of seat-backs receding to the front.
 */
@Composable
fun CabinAisleScene(longitude: Double, modifier: Modifier = Modifier) {
    val hour = remember(longitude) { localSolarHour(Instant.now(), longitude) }
    val palette = skyPaletteFor(hour)
    Box(modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(26.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            val view = CabinView(size.width / 2, size.height * 0.42f, size.width * 0.5f, size.height)
            drawCabinShell(view)
            (7 downTo 0).forEach { drawRowAt(CabinRow(it / 8f, view), palette) }
            drawCabinGlow(palette.night)
        }
    }
}

private fun DrawScope.drawCabinShell(view: CabinView) {
    val far = 0.16f
    val farHalf = view.halfWidth * far
    val topFar = view.vanishY - size.height * 0.2f * far * 2
    val bottomFar = view.vanishY + size.height * 0.2f * far * 2
    drawRect(Brush.verticalGradient(listOf(CabinBin, CabinWall)), size = size)
    drawWall(Path().apply {
        moveTo(0f, 0f); lineTo(view.vanishX - farHalf, topFar); lineTo(view.vanishX - farHalf, bottomFar); lineTo(0f, view.floorY); close()
    })
    drawWall(Path().apply {
        moveTo(size.width, 0f); lineTo(view.vanishX + farHalf, topFar); lineTo(view.vanishX + farHalf, bottomFar); lineTo(size.width, view.floorY); close()
    })
    drawCarpet(view, farHalf, bottomFar)
    drawBins(view, farHalf, topFar)
}

private fun DrawScope.drawWall(path: Path) {
    drawPath(path, Brush.horizontalGradient(listOf(Color(0xFFDDD5C4), CabinWall), 0f, size.width))
}

private fun DrawScope.drawCarpet(view: CabinView, farHalf: Float, bottomFar: Float) {
    val carpet = Path().apply {
        moveTo(view.vanishX - farHalf * 0.35f, bottomFar)
        lineTo(view.vanishX + farHalf * 0.35f, bottomFar)
        lineTo(view.vanishX + view.halfWidth * 0.34f, view.floorY)
        lineTo(view.vanishX - view.halfWidth * 0.34f, view.floorY)
        close()
    }
    drawPath(carpet, Brush.verticalGradient(listOf(CabinCarpet.copy(alpha = 0.7f), CabinCarpet), bottomFar, view.floorY))
}

private fun DrawScope.drawBins(view: CabinView, farHalf: Float, topFar: Float) {
    val leftBin = Path().apply {
        moveTo(0f, 0f); lineTo(view.vanishX - farHalf, topFar); lineTo(view.vanishX - farHalf, topFar + size.height * 0.05f); lineTo(0f, size.height * 0.2f); close()
    }
    val rightBin = Path().apply {
        moveTo(size.width, 0f); lineTo(view.vanishX + farHalf, topFar); lineTo(view.vanishX + farHalf, topFar + size.height * 0.05f); lineTo(size.width, size.height * 0.2f); close()
    }
    drawPath(leftBin, CabinBin)
    drawPath(rightBin, CabinBin)
}

/** Draws both seat blocks, the window and the reading-light strip at one depth. */
private fun DrawScope.drawRowAt(row: CabinRow, palette: SkyPalette) {
    val scale = 1f - row.depth * 0.86f
    val view = row.view
    val y = view.vanishY + (view.floorY - view.vanishY) * scale * 0.98f
    val half = view.halfWidth * scale
    val seatW = half * 0.26f
    val seatH = size.height * 0.2f * scale
    drawWindow(Offset(view.vanishX - half * 0.98f, y - seatH * 2.1f), Size(half * 0.13f, seatH * 1.1f), palette)
    drawWindow(Offset(view.vanishX + half * 0.85f, y - seatH * 2.1f), Size(half * 0.13f, seatH * 1.1f), palette)
    drawSeatBlock(Offset(view.vanishX - half * 0.78f, y - seatH), Size(seatW * 1.9f, seatH))
    drawSeatBlock(Offset(view.vanishX + half * 0.78f - seatW * 1.9f, y - seatH), Size(seatW * 1.9f, seatH))
}

private fun DrawScope.drawWindow(topLeft: Offset, size: Size, palette: SkyPalette) {
    drawRoundRect(Color(0xFFCFC7B6), topLeft, size, CornerRadius(size.width * 0.5f))
    val inset = size.width * 0.14f
    drawRoundRect(
        Brush.verticalGradient(listOf(palette.top, palette.horizon), topLeft.y, topLeft.y + size.height),
        Offset(topLeft.x + inset, topLeft.y + inset),
        Size(size.width - inset * 2, size.height - inset * 2),
        CornerRadius(size.width * 0.4f),
    )
}

private fun DrawScope.drawSeatBlock(topLeft: Offset, size: Size) {
    drawRoundRect(SeatCloth, topLeft, size, CornerRadius(size.width * 0.18f))
    drawRoundRect(SeatHeadrest, Offset(topLeft.x + size.width * 0.16f, topLeft.y + size.height * 0.04f), Size(size.width * 0.68f, size.height * 0.2f), CornerRadius(size.width * 0.1f))
}

/** A warm cabin light at day, dimmed and bluer at night. */
private fun DrawScope.drawCabinGlow(night: Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0x33FFE9C7), Color.Transparent), 0f, size.height * 0.5f))
    if (night > 0.3f) drawRect(Color(0xFF0B1022).copy(alpha = 0.35f * night), size = size)
}
