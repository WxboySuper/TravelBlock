package com.travelblock.app.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import com.travelblock.app.domain.model.GeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Both styles are the standard OpenStreetMap tiles, re-coloured in code (warm day / inverted night). */
enum class TravelMapStyle { Daylight, Night }

/** One end of a route as shown on the map. */
data class MapStop(val code: String, val point: GeoPoint)

data class RouteStops(val origin: MapStop, val destination: MapStop)

data class MapInsets(val top: Int = 0, val bottom: Int = 0)

data class MapOptions(
    val style: TravelMapStyle = TravelMapStyle.Daylight,
    val interactive: Boolean = false,
    val insets: MapInsets = MapInsets(),
)

private data class MapPoint(val x: Float, val y: Float)

private data class PlanePosition(val point: MapPoint, val heading: Float)

/**
 * A real, tile-based map showing the true great-circle route between two airports.
 *
 * @param progress 0..1 position of the aircraft along the route, or null for a planned route with no plane.
 * @param bottomInset space (in dp) to keep clear at the bottom, e.g. under a sheet, so the route stays visible.
 */
@Composable
fun TravelMap(
    stops: RouteStops,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    options: MapOptions = MapOptions(),
) {
    val context = LocalContext.current
    remember { configureOsmdroid(context) }
    val routeOverlay = remember(stops, options.style) { RouteOverlay(stops, options.style) }
    routeOverlay.progress = progress

    Box(modifier.clipToBounds()) {
        val mapView = remember(options.style) { createMapView(context, options.style) }
        DisposableEffect(mapView) {
            mapView.onResume()
            onDispose {
                mapView.onPause()
                mapView.onDetach()
            }
        }
        val fitKey = "${stops.origin.code}-${stops.destination.code}-${options.insets.top}-${options.insets.bottom}"
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    setMultiTouchControls(options.interactive)
                    setOnTouchListener(if (options.interactive) null else { _, _ -> true })
                    overlays.add(routeOverlay)
                }
            },
            update = { view ->
                if (!view.overlays.contains(routeOverlay)) {
                    view.overlays.removeAll { it is RouteOverlay }
                    view.overlays.add(routeOverlay)
                }
                if (view.tag != fitKey) {
                    view.tag = fitKey
                    view.fitRoute(routeOverlay.arc, options.insets)
                }
                view.invalidate()
            },
        )
        Text(
            text = "(c) OpenStreetMap contributors",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, textAlign = TextAlign.End),
            color = if (options.style == TravelMapStyle.Night) Color(0x99FFFFFF) else Color(0x99000000),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = (options.insets.bottom + 4).dp),
        )
    }
}

internal fun configureOsmdroid(context: Context) {
    val config = Configuration.getInstance()
    config.userAgentValue = context.packageName
    val cache = java.io.File(context.cacheDir, "osmdroid").apply { mkdirs() }
    config.osmdroidBasePath = cache
    config.osmdroidTileCache = java.io.File(cache, "tiles").apply { mkdirs() }
    config.tileFileSystemCacheMaxBytes = 80L * 1024 * 1024
    config.tileFileSystemCacheTrimBytes = 60L * 1024 * 1024
}

internal fun createMapView(context: Context, style: TravelMapStyle): MapView = MapView(context).apply {
    setTileSource(TileSourceFactory.MAPNIK)
    setBuiltInZoomControls(false)
    isTilesScaledToDpi = true
    minZoomLevel = 2.0
    maxZoomLevel = 12.0
    // Re-colour the standard tiles so the map feels like part of the journal, not a stock web map.
    val filter = if (style == TravelMapStyle.Night) {
        // Invert to a deep navy night map, keeping labels readable.
        ColorMatrix(floatArrayOf(-0.9f, 0f, 0f, 0f, 238f, 0f, -0.9f, 0f, 0f, 242f, 0f, 0f, -0.9f, 0f, 255f, 0f, 0f, 0f, 1f, 0f))
    } else {
        // Soften saturation, then warm toward paper.
        ColorMatrix().apply {
            setSaturation(0.7f)
            postConcat(ColorMatrix(floatArrayOf(1.04f, 0.02f, 0f, 0f, 6f, 0f, 1.0f, 0f, 0f, 3f, 0f, 0f, 0.9f, 0f, 0f, 0f, 0f, 0f, 1f, 0f)))
        }
    }
    overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(filter))
}

private fun MapView.fitRoute(arc: List<GeoPoint>, insets: MapInsets) {
    val points = arc.map { org.osmdroid.util.GeoPoint(it.latitude, it.longitude) }
    val route = BoundingBox.fromGeoPoints(points)
    val density = resources.displayMetrics.density
    post {
        val total = height.toFloat().coerceAtLeast(1f)
        val top = insets.top * density
        val bottom = insets.bottom * density
        val visible = (total - top - bottom).coerceAtLeast(total * 0.25f)
        // Stretch the box north/south so the route lands in the band that no header or sheet covers.
        val span = route.latitudeSpan.coerceAtLeast(0.02)
        val north = (route.latNorth + span * (top / visible)).coerceAtMost(85.0)
        val south = (route.latSouth - span * (bottom / visible)).coerceAtLeast(-85.0)
        val fitted = BoundingBox(north, route.lonEast, south, route.lonWest)
        zoomToBoundingBox(fitted, false, (36 * density).toInt())
    }
}

/** Draws the route arc, airport markers and the aircraft directly in map coordinates. */
private class RouteOverlay(
    private val stops: RouteStops,
    private val style: TravelMapStyle,
) : Overlay() {
    var progress: Float? = null
    val arc: List<GeoPoint> = FlightRouteInterpolation.buildRoute(stops.origin.point, stops.destination.point, 96)

    private val night = style == TravelMapStyle.Night
    private val ink = if (night) 0xFFFAF4E8.toInt() else 0xFF25304A.toInt()
    private val accent = if (night) 0xFFF2C46D.toInt() else 0xFFCF6F4A.toInt()
    private val paper = if (night) 0xFF1E2740.toInt() else 0xFFFFFBF3.toInt()

    private val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        color = paper
        alpha = 200
        strokeCap = Paint.Cap.ROUND
    }
    private val dashed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = accent
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(1f, 22f), 0f)
    }
    private val solid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = accent
        strokeCap = Paint.Cap.ROUND
    }
    private val dotFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper }
    private val dotRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.STROKE; strokeWidth = 6f; color = ink
    }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink; textSize = 38f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER
    }
    private val labelBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper; setShadowLayer(8f, 0f, 3f, 0x33000000) }
    private val planeFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
    private val planeRing = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val projection = mapView.projection
        val pts = arc.map { projection.toPixels(org.osmdroid.util.GeoPoint(it.latitude, it.longitude), null) }
        val frac = progress?.coerceIn(0f, 1f)
        val split = ((arc.size - 1) * (frac ?: 0f)).toInt().coerceIn(0, arc.size - 1)

        val remaining = Path().apply {
            pts.drop(split).forEachIndexed { i, p -> if (i == 0) moveTo(p.x.toFloat(), p.y.toFloat()) else lineTo(p.x.toFloat(), p.y.toFloat()) }
        }
        canvas.drawPath(remaining, halo)
        canvas.drawPath(remaining, dashed)
        if (frac != null && split > 0) {
            val done = Path().apply {
                pts.take(split + 1).forEachIndexed { i, p -> if (i == 0) moveTo(p.x.toFloat(), p.y.toFloat()) else lineTo(p.x.toFloat(), p.y.toFloat()) }
            }
            canvas.drawPath(done, solid)
        }

        drawStop(canvas, MapPoint(pts.first().x.toFloat(), pts.first().y.toFloat()), stops.origin.code)
        drawStop(canvas, MapPoint(pts.last().x.toFloat(), pts.last().y.toFloat()), stops.destination.code)

        if (frac != null) {
            val aircraft = FlightRouteInterpolation.interpolate(stops.origin.point, stops.destination.point, frac.toDouble())
            val ahead = FlightRouteInterpolation.interpolate(stops.origin.point, stops.destination.point, (frac + 0.01f).coerceAtMost(1f).toDouble())
            val p = projection.toPixels(org.osmdroid.util.GeoPoint(aircraft.latitude, aircraft.longitude), null)
            val q = projection.toPixels(org.osmdroid.util.GeoPoint(ahead.latitude, ahead.longitude), null)
            val heading = Math.toDegrees(atan2((q.x - p.x).toDouble(), -(q.y - p.y).toDouble())).toFloat()
            drawPlane(canvas, PlanePosition(MapPoint(p.x.toFloat(), p.y.toFloat()), heading))
        }
    }

    private fun drawStop(canvas: Canvas, point: MapPoint, code: String) {
        val (x, y) = point
        canvas.drawCircle(x, y, 17f, dotFill)
        canvas.drawCircle(x, y, 17f, dotRing)
        val w = label.measureText(code) + 40f
        val rect = RectF(x - w / 2, y - 98f, x + w / 2, y - 44f)
        canvas.drawRoundRect(rect, 27f, 27f, labelBg)
        canvas.drawText(code, x, y - 59f, label)
    }

    private fun drawPlane(canvas: Canvas, position: PlanePosition) {
        val (x, y) = position.point
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(position.heading)
        canvas.drawCircle(0f, 0f, 34f, planeRing)
        val body = Path().apply {
            moveTo(0f, -26f)
            cubicTo(5f, -22f, 6f, -10f, 6f, 0f)
            lineTo(26f, 11f); lineTo(26f, 16f); lineTo(6f, 10f)
            lineTo(5f, 20f); lineTo(12f, 25f); lineTo(12f, 28f); lineTo(0f, 24f)
            lineTo(-12f, 28f); lineTo(-12f, 25f); lineTo(-5f, 20f)
            lineTo(-6f, 10f); lineTo(-26f, 16f); lineTo(-26f, 11f); lineTo(-6f, 0f)
            cubicTo(-6f, -10f, -5f, -22f, 0f, -26f)
            close()
        }
        canvas.drawPath(body, planeFill)
        canvas.restore()
    }
}
