package com.travelblock.app.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.ui.theme.ActivePalette
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/** One selectable destination pin on the departures map. */
data class DeparturePin(val id: String, val code: String, val point: GeoPoint)

/** What the departures map shows: where you are, every destination you could fly to, and which is chosen. */
data class DeparturesMapModel(val origin: MapStop, val pins: List<DeparturePin>, val selectedId: String?)

/**
 * An interactive map of the departures board: tap a pin to choose that destination, and its great-circle route
 * is drawn from your airport. Pan and pinch to look around.
 */
@Composable
fun DeparturesMap(
    model: DeparturesMapModel,
    onPinSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TravelMapStyle = TravelMapStyle.Daylight,
) {
    val context = LocalContext.current
    remember { configureOsmdroid(context) }
    val mapView = remember(style) { createMapView(context, style) }
    val overlay = remember(style) { DeparturesOverlay(style) }
    overlay.model = model
    overlay.onSelect = onPinSelected
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }
    Box(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    setMultiTouchControls(true)
                    // The map sits inside a scrolling page: keep drags for the map.
                    setOnTouchListener { view, event ->
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) view.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    overlays.add(overlay)
                }
            },
            update = { view ->
                val key = model.origin.code + "|" + model.pins.joinToString(",") { it.id }
                if (view.tag != key) {
                    view.tag = key
                    view.fitPins(model)
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

private fun MapView.fitPins(model: DeparturesMapModel) {
    val points = (model.pins.map { it.point } + model.origin.point).map { org.osmdroid.util.GeoPoint(it.latitude, it.longitude) }
    val density = resources.displayMetrics.density
    post {
        var box = BoundingBox.fromGeoPoints(points)
        if (box.latitudeSpan < 0.4 && box.longitudeSpan < 0.4) {
            val c = box.centerWithDateLine
            box = BoundingBox(c.latitude + 0.35, c.longitude + 0.35, c.latitude - 0.35, c.longitude - 0.35)
        }
        zoomToBoundingBox(box, false, (44 * density).toInt())
    }
}

private fun MapView.pixelOf(point: GeoPoint): Pair<Float, Float> {
    val p = projection.toPixels(org.osmdroid.util.GeoPoint(point.latitude, point.longitude), null)
    return p.x.toFloat() to p.y.toFloat()
}

private class DeparturesOverlay(style: TravelMapStyle) : Overlay() {
    var model: DeparturesMapModel? = null
    var onSelect: (String) -> Unit = {}

    private val night = style == TravelMapStyle.Night
    private val palette = ActivePalette.current
    private val ink = (if (night) palette.cream else palette.ink).toArgb()
    private val paper = (if (night) palette.nightSurface else palette.paper).toArgb()
    private val accent = (if (night) palette.lamp else palette.accent).toArgb()

    private val routeHalo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.STROKE; color = paper; strokeWidth = 12f; strokeCap = Paint.Cap.ROUND; alpha = 190
    }
    private val route = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.STROKE; color = accent; strokeWidth = 6f; strokeCap = Paint.Cap.ROUND
    }
    private val pinFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.duskBlue.toArgb() }
    private val pinSelected = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
    private val pinRing = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.style = Paint.Style.STROKE; strokeWidth = 4f; color = paper }
    private val originFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper }
    private val originRing = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.style = Paint.Style.STROKE; strokeWidth = 6f; color = ink }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink; textSize = 30f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER
    }
    private val labelBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper; setShadowLayer(6f, 0f, 2f, 0x33000000) }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        val m = model ?: return
        if (shadow) return
        val selected = m.pins.firstOrNull { it.id == m.selectedId }
        selected?.let { drawRoute(canvas, mapView, m.origin.point, it.point) }
        m.pins.filter { it !== selected }.forEach { drawPin(canvas, mapView, it, isSelected = false) }
        selected?.let { drawPin(canvas, mapView, it, isSelected = true) }
        val (ox, oy) = mapView.pixelOf(m.origin.point)
        canvas.drawCircle(ox, oy, 13f, originFill)
        canvas.drawCircle(ox, oy, 13f, originRing)
        drawTag(canvas, ox, oy, m.origin.code)
    }

    private fun drawRoute(canvas: Canvas, mapView: MapView, from: GeoPoint, to: GeoPoint) {
        val path = Path()
        FlightRouteInterpolation.buildRoute(from, to, 96).forEachIndexed { i, point ->
            val (x, y) = mapView.pixelOf(point)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, routeHalo)
        canvas.drawPath(path, route)
    }

    private fun drawPin(canvas: Canvas, mapView: MapView, pin: DeparturePin, isSelected: Boolean) {
        val (x, y) = mapView.pixelOf(pin.point)
        val radius = if (isSelected) 15f else 9f
        canvas.drawCircle(x, y, radius, if (isSelected) pinSelected else pinFill)
        canvas.drawCircle(x, y, radius, pinRing)
        if (isSelected) drawTag(canvas, x, y, pin.code)
    }

    private fun drawTag(canvas: Canvas, x: Float, y: Float, text: String) {
        val w = label.measureText(text) + 28f
        canvas.drawRoundRect(RectF(x - w / 2, y - 66f, x + w / 2, y - 30f), 18f, 18f, labelBg)
        canvas.drawText(text, x, y - 40f, label)
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        val m = model ?: return false
        val reach = 36f * mapView.resources.displayMetrics.density
        val nearest = m.pins.map { pin ->
            val (x, y) = mapView.pixelOf(pin.point)
            pin to (x - e.x) * (x - e.x) + (y - e.y) * (y - e.y)
        }.minByOrNull { it.second } ?: return false
        if (nearest.second > reach * reach) return false
        onSelect(nearest.first.id)
        return true
    }
}
