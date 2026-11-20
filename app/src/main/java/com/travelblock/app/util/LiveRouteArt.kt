package com.travelblock.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.GeoPoint

/**
 * The picture beside the live notification: a small drawn map of this flight. The route is a great-circle arc between
 * the two airports with the part already flown in bright colour, both airport codes, and the plane where it is now.
 * A promoted notification cannot use a custom layout, but it can show an image, so the map is the image.
 */
object LiveRouteArt {
    private const val SIZE = 288
    private const val PADDING = 44f
    private const val SAMPLES = 40
    private const val CORNER = 56f
    private const val BACKGROUND = 0xFF0E1A33.toInt()
    private const val ROUTE = 0x66B9C6FF
    private const val FLOWN = 0xFF4F6BFF.toInt()
    private const val SIGNAL = 0xFF14C3BA.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val MIN_SPAN_DEGREES = 0.2

    fun render(flight: ActiveFlight, progress: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRoundRect(RectF(0f, 0f, SIZE.toFloat(), SIZE.toFloat()), CORNER, CORNER, fill(BACKGROUND))
        val route = FlightRouteInterpolation.buildRoute(
            GeoPoint(flight.origin.latitude, flight.origin.longitude),
            GeoPoint(flight.destination.latitude, flight.destination.longitude),
            SAMPLES,
        )
        val projection = Projection.fit(route)
        val points = route.map(projection::toPixel)
        val flown = (progress.coerceIn(0f, 1f) * (points.size - 1)).toInt()
        canvas.drawPath(pathOf(points), stroke(ROUTE, 7f))
        canvas.drawPath(pathOf(points.take(flown + 1)), stroke(FLOWN, 9f))
        drawAirport(canvas, points.first(), flight.origin.displayCode, labelAbove = true)
        drawAirport(canvas, points.last(), flight.destination.displayCode, labelAbove = false)
        drawPlane(canvas, points[flown])
        return bitmap
    }

    private fun drawAirport(canvas: Canvas, at: Pair<Float, Float>, code: String, labelAbove: Boolean) {
        canvas.drawCircle(at.first, at.second, 11f, fill(WHITE))
        val y = if (labelAbove) at.second - 22f else at.second + 40f
        canvas.drawText(code, at.first.coerceIn(48f, SIZE - 48f), y.coerceIn(36f, SIZE - 18f), label())
    }

    private fun drawPlane(canvas: Canvas, at: Pair<Float, Float>) {
        canvas.drawCircle(at.first, at.second, 19f, fill(WHITE))
        canvas.drawCircle(at.first, at.second, 13f, fill(SIGNAL))
    }

    private fun pathOf(points: List<Pair<Float, Float>>): Path = Path().apply {
        points.forEachIndexed { index, (x, y) -> if (index == 0) moveTo(x, y) else lineTo(x, y) }
    }

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

    private fun stroke(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private fun label() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WHITE
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    /** Fits the route into the square keeping its shape, centred, with room for the labels. */
    private class Projection(private val centre: GeoPoint, private val scale: Double) {
        fun toPixel(point: GeoPoint): Pair<Float, Float> = Pair(
            (SIZE / 2 + (point.longitude - centre.longitude) * scale).toFloat(),
            (SIZE / 2 - (point.latitude - centre.latitude) * scale).toFloat(),
        )

        companion object {
            fun fit(route: List<GeoPoint>): Projection {
                val width = (route.maxOf { it.longitude } - route.minOf { it.longitude }).coerceAtLeast(MIN_SPAN_DEGREES)
                val height = (route.maxOf { it.latitude } - route.minOf { it.latitude }).coerceAtLeast(MIN_SPAN_DEGREES)
                val centre = GeoPoint(
                    (route.maxOf { it.latitude } + route.minOf { it.latitude }) / 2,
                    (route.maxOf { it.longitude } + route.minOf { it.longitude }) / 2,
                )
                return Projection(centre, (SIZE - 2 * PADDING) / maxOf(width, height))
            }
        }
    }
}
