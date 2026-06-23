package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.FlightMapModel
import com.travelblock.app.domain.model.GeoPoint
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object FlightRouteInterpolation {
    fun mapModelForFlight(
        activeFlight: ActiveFlight,
        progress: Double,
        sampleCount: Int = 32,
    ): FlightMapModel {
        val route = buildRoute(
            origin = GeoPoint(activeFlight.origin.latitude, activeFlight.origin.longitude),
            destination = GeoPoint(activeFlight.destination.latitude, activeFlight.destination.longitude),
            sampleCount = sampleCount,
        )
        return FlightMapModel(
            route = route,
            aircraftPosition = interpolate(route.first(), route.last(), progress),
            progress = progress.coerceIn(0.0, 1.0),
        )
    }

    fun buildRoute(
        origin: GeoPoint,
        destination: GeoPoint,
        sampleCount: Int = 32,
    ): List<GeoPoint> {
        val count = sampleCount.coerceAtLeast(2)
        return List(count) { index ->
            interpolate(origin, destination, index.toDouble() / (count - 1).toDouble())
        }
    }

    fun interpolate(
        origin: GeoPoint,
        destination: GeoPoint,
        progress: Double,
    ): GeoPoint {
        val t = progress.coerceIn(0.0, 1.0)
        if (t == 0.0) return origin
        if (t == 1.0) return destination

        val a = toCartesian(origin)
        val b = toCartesian(destination)
        val dot = (a.x * b.x + a.y * b.y + a.z * b.z).coerceIn(-1.0, 1.0)
        val omega = acos(dot)

        if (omega == 0.0 || !omega.isFinite()) {
            return GeoPoint(
                latitude = lerp(origin.latitude, destination.latitude, t),
                longitude = lerp(origin.longitude, destination.longitude, t),
            )
        }

        val sinOmega = sin(omega)
        val scaleA = sin((1.0 - t) * omega) / sinOmega
        val scaleB = sin(t * omega) / sinOmega
        val interpolated = Vec3(
            x = scaleA * a.x + scaleB * b.x,
            y = scaleA * a.y + scaleB * b.y,
            z = scaleA * a.z + scaleB * b.z,
        ).normalized()

        val latitude = Math.toDegrees(kotlin.math.asin(interpolated.z))
        val longitude = Math.toDegrees(kotlin.math.atan2(interpolated.y, interpolated.x))
        return GeoPoint(latitude = latitude, longitude = normalizeLongitude(longitude))
    }

    private fun lerp(start: Double, end: Double, t: Double): Double = start + (end - start) * t

    private fun normalizeLongitude(longitude: Double): Double {
        var value = longitude
        while (value > 180.0) value -= 360.0
        while (value < -180.0) value += 360.0
        return value
    }

    private fun toCartesian(point: GeoPoint): Vec3 {
        val lat = Math.toRadians(point.latitude)
        val lon = Math.toRadians(point.longitude)
        return Vec3(
            x = cos(lat) * cos(lon),
            y = cos(lat) * sin(lon),
            z = sin(lat),
        )
    }

    private data class Vec3(
        val x: Double,
        val y: Double,
        val z: Double,
    ) {
        fun normalized(): Vec3 {
            val magnitude = sqrt(x.pow(2) + y.pow(2) + z.pow(2))
            if (magnitude == 0.0 || !magnitude.isFinite()) return this
            return Vec3(
                x = x / magnitude,
                y = y / magnitude,
                z = z / magnitude,
            )
        }
    }
}
