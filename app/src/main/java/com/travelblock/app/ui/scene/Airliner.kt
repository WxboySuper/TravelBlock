package com.travelblock.app.ui.scene

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform

/** Colours of the airliner: body, shaded underside/wing, livery accent (tail) and window glass. */
data class AirlinerStyle(val body: Color, val shade: Color, val accent: Color, val window: Color)

/** Which way the plane faces (nose left by default) and how far it is pitched (positive = nose down). */
data class AirlinerPose(val facingRight: Boolean = false, val pitchDegrees: Float = 0f, val gearDown: Boolean = false)

/**
 * A side-on airliner drawn in a unit box (nose at x=0, tail at x=1, centre line at y=0), then scaled to
 * [length] and centred on [center]. Every part is attached to the fuselage: nose cone, tapering tail cone,
 * vertical fin, swept wing with an engine slung under it, and a row of windows.
 */
fun DrawScope.drawAirliner(center: Offset, length: Float, style: AirlinerStyle, pose: AirlinerPose = AirlinerPose()) {
    withTransform({
        translate(center.x, center.y)
        rotate(if (pose.facingRight) -pose.pitchDegrees else pose.pitchDegrees, pivot = Offset.Zero)
        scale(if (pose.facingRight) -length else length, length, pivot = Offset.Zero)
        translate(-0.5f, 0f)
    }) {
        if (pose.gearDown) drawLandingGear(style)
        drawTailFin(style)
        drawWing(style)
        drawFuselage(style)
        drawWindows(style)
    }
}

private fun DrawScope.drawFuselage(style: AirlinerStyle) {
    val body = Path().apply {
        moveTo(0.0f, 0.01f)
        cubicTo(0.0f, -0.045f, 0.05f, -0.07f, 0.13f, -0.07f)
        lineTo(0.76f, -0.07f)
        cubicTo(0.88f, -0.07f, 0.96f, -0.045f, 1.0f, -0.025f)
        lineTo(1.0f, 0.0f)
        cubicTo(0.95f, 0.045f, 0.86f, 0.07f, 0.74f, 0.07f)
        lineTo(0.13f, 0.07f)
        cubicTo(0.05f, 0.07f, 0.0f, 0.045f, 0.0f, 0.01f)
        close()
    }
    drawPath(body, style.body)
    // Belly shading and a thin livery line along the cabin.
    drawRect(style.shade, Offset(0.14f, 0.05f), Size(0.58f, 0.018f))
    drawRect(style.accent, Offset(0.06f, 0.02f), Size(0.84f, 0.008f))
}

private fun DrawScope.drawTailFin(style: AirlinerStyle) {
    val fin = Path().apply {
        moveTo(0.76f, -0.065f)
        lineTo(0.92f, -0.25f)
        lineTo(1.0f, -0.25f)
        lineTo(1.0f, -0.03f)
        close()
    }
    drawPath(fin, style.accent)
    // Small horizontal stabiliser sticking out behind the fin.
    drawRoundRect(style.shade, Offset(0.9f, -0.03f), Size(0.13f, 0.014f), CornerRadius(0.007f))
}

private fun DrawScope.drawWing(style: AirlinerStyle) {
    val wing = Path().apply {
        moveTo(0.36f, 0.035f)
        lineTo(0.58f, 0.035f)
        lineTo(0.45f, 0.17f)
        lineTo(0.38f, 0.17f)
        close()
    }
    drawPath(wing, style.shade)
    // Engine nacelle hung under the wing.
    drawRoundRect(style.shade, Offset(0.36f, 0.095f), Size(0.11f, 0.06f), CornerRadius(0.03f))
    drawRoundRect(style.body.copy(alpha = 0.55f), Offset(0.362f, 0.108f), Size(0.012f, 0.034f), CornerRadius(0.006f))
}

private fun DrawScope.drawWindows(style: AirlinerStyle) {
    for (i in 0..11) {
        drawCircle(style.window, 0.0105f, Offset(0.2f + i * 0.047f, -0.025f))
    }
    // Cockpit windscreen on the nose.
    val screen = Path().apply {
        moveTo(0.055f, -0.052f)
        lineTo(0.115f, -0.058f)
        lineTo(0.105f, -0.028f)
        lineTo(0.04f, -0.026f)
        close()
    }
    drawPath(screen, style.window)
}

private fun DrawScope.drawLandingGear(style: AirlinerStyle) {
    listOf(0.17f, 0.58f).forEach { x ->
        drawRect(style.shade, Offset(x - 0.006f, 0.05f), Size(0.012f, 0.06f))
        drawCircle(style.window, 0.017f, Offset(x, 0.112f))
    }
}
