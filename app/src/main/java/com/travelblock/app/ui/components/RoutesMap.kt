package com.travelblock.app.ui.components

import android.graphics.Canvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/** One flown leg between two airports. */
data class RouteLeg(val origin: MapStop, val destination: MapStop)

/**
 * A static map of everywhere you've flown: every leg drawn as a great-circle arc, thicker and more opaque
 * where the same route repeats, with a small marker and code for each airport you've touched.
 */
@Composable
fun RoutesMap(
    legs: List<RouteLeg>,
    modifier: Modifier = Modifier,
    style: TravelMapStyle = TravelMapStyle.Daylight,
) {
    val context = LocalContext.current
    remember { configureOsmdroid(context) }
    val overlay = remember(legs, style) { RoutesOverlay(legs, style) }

    Box(modifier) {
        val mapView = remember(style) { createMapView(context, style) }
        DisposableEffect(mapView) {
            mapView.onResume()
            onDispose {
                mapView.onPause()
                mapView.onDetach()
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    setMultiTouchControls(false)
                    // Display-only: let the surrounding list keep its scroll gestures.
                    setOnTouchListener { _, _ -> false }
                    overlays.add(overlay)
                }
            },
            update = { view ->
                if (!view.overlays.contains(overlay)) {
                    view.overlays.removeAll { it is RoutesOverlay }
                    view.overlays.add(overlay)
                }
                val key = legs.joinToString("|") { "${it.origin.code}-${it.destination.code}" }
                if (view.tag != key) {
                    view.tag = key
                    view.fitAll(overlay.allPoints())
                }
                view.invalidate()
            },
        )
        Text(
            text = "(c) OpenStreetMap contributors",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = if (style == TravelMapStyle.Night) Color(0x99FFFFFF) else Color(0x99000000),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 4.dp),
        )
    }
}

private const val REFIT_DELAY_MS = 400L
private const val RETRY_DELAY_MS = 50L
private const val MAX_RETRIES = 40

/**
 * Frames every route. A map built while the list is still scrolling has no size yet, and fitting then leaves it
 * centred in the wrong place, so wait until it is attached and measured, fit, and fit once more after it settles.
 */
private fun MapView.fitAll(points: List<org.osmdroid.util.GeoPoint>) {
    if (points.isEmpty()) return
    val density = resources.displayMetrics.density
    val fit = {
        var box = BoundingBox.fromGeoPoints(points)
        // A single short hop (or one airport) would otherwise zoom to the maximum level.
        if (box.latitudeSpan < 0.4 && box.longitudeSpan < 0.4) {
            val c = box.centerWithDateLine
            box = BoundingBox(c.latitude + 0.35, c.longitude + 0.35, c.latitude - 0.35, c.longitude - 0.35)
        }
        zoomToBoundingBox(box, false, (40 * density).toInt())
    }
    fun attempt(tries: Int) {
        if (width > 0 && height > 0 && isAttachedToWindow) {
            fit()
            postDelayed({ fit() }, REFIT_DELAY_MS)
        } else if (tries < MAX_RETRIES) {
            postDelayed({ attempt(tries + 1) }, RETRY_DELAY_MS)
        }
    }
    attempt(0)
}

private class RoutesOverlay(legs: List<RouteLeg>, style: TravelMapStyle) : Overlay() {
    private class Arc(val points: List<org.osmdroid.util.GeoPoint>, val count: Int)

    private val arcs: List<Arc>
    private val stops: List<MapStop>

    init {
        val byKey = legs.groupBy { listOf(it.origin.code, it.destination.code).sorted().joinToString("-") }
        arcs = byKey.values.map { group ->
            val leg = group.first()
            val route = FlightRouteInterpolation.buildRoute(leg.origin.point, leg.destination.point, 64)
            Arc(route.map { org.osmdroid.util.GeoPoint(it.latitude, it.longitude) }, group.size)
        }
        stops = legs.flatMap { listOf(it.origin, it.destination) }.distinctBy { it.code }
    }

    fun allPoints(): List<org.osmdroid.util.GeoPoint> = arcs.flatMap { it.points }

    private val night = style == TravelMapStyle.Night
    private val palette = com.travelblock.app.ui.theme.ActivePalette.current
    private val ink = (if (night) palette.cream else palette.ink).toArgb()
    private val accent = (if (night) palette.lamp else palette.accent).toArgb()
    private val paper = (if (night) palette.nightSurface else palette.paper).toArgb()

    private val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.STROKE
        color = paper
        strokeCap = Paint.Cap.ROUND
    }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.STROKE
        color = accent
        strokeCap = Paint.Cap.ROUND
    }
    private val dotFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper }
    private val dotRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.STROKE; strokeWidth = 4f; color = ink
    }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink; textSize = 30f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER
    }
    private val labelBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper; setShadowLayer(6f, 0f, 2f, 0x33000000) }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val projection = mapView.projection
        arcs.forEach { arc ->
            val path = Path()
            arc.points.forEachIndexed { i, geo ->
                val p = projection.toPixels(geo, null)
                if (i == 0) path.moveTo(p.x.toFloat(), p.y.toFloat()) else path.lineTo(p.x.toFloat(), p.y.toFloat())
            }
            val width = 5f + (arc.count - 1).coerceAtMost(4) * 3f
            halo.strokeWidth = width + 6f
            halo.alpha = 170
            line.strokeWidth = width
            line.alpha = (150 + (arc.count - 1).coerceAtMost(4) * 25).coerceAtMost(255)
            canvas.drawPath(path, halo)
            canvas.drawPath(path, line)
        }
        val showLabels = stops.size <= 14
        stops.forEach { stop ->
            val p = projection.toPixels(org.osmdroid.util.GeoPoint(stop.point.latitude, stop.point.longitude), null)
            val x = p.x.toFloat()
            val y = p.y.toFloat()
            canvas.drawCircle(x, y, 11f, dotFill)
            canvas.drawCircle(x, y, 11f, dotRing)
            if (showLabels) {
                val w = label.measureText(stop.code) + 28f
                canvas.drawRoundRect(RectF(x - w / 2, y - 64f, x + w / 2, y - 28f), 18f, 18f, labelBg)
                canvas.drawText(stop.code, x, y - 38f, label)
            }
        }
    }
}
