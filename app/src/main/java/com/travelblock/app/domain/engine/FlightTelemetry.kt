package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import java.time.Duration
import java.time.Instant
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class FlightStage(val label: String) {
    Climbing("Climbing"),
    Cruising("Cruising"),
    Descending("Descending"),
}

/** The numbers on a seat-back flight display: where the plane is in its climb, and what the instruments read. */
data class FlightTelemetry(
    val stage: FlightStage,
    val altitudeFeet: Int,
    val groundSpeedMph: Int,
    val outsideTempF: Int,
    val headingDegrees: Int,
    val originLocalTime: String,
    val destinationLocalTime: String,
)

/**
 * Simulated instruments for a flight. The route and timing are real to the app (360 mph cruise); altitude, speed and
 * temperature follow a believable profile: climb, cruise at an altitude suited to the distance, then descend.
 * Pure and deterministic, so it can be tested and shown to the second.
 */
object FlightTelemetryModel {
    private const val CRUISE_SPEED_MPH = 360
    private const val TAKEOFF_SPEED_MPH = 150
    private const val APPROACH_SPEED_MPH = 140
    private const val SEA_LEVEL_TEMP_F = 59.0
    private const val LAPSE_F_PER_THOUSAND_FEET = 3.57
    private const val COLDEST_TEMP_F = -69
    private const val MIN_RAMP_FRACTION = 0.1
    private const val MAX_RAMP_FRACTION = 0.25
    private const val TARGET_RAMP_MINUTES = 12.0
    private const val SHORT_HOP_MILES = 150.0
    private const val REGIONAL_MILES = 500.0
    private const val SHORT_HOP_FEET = 18_000
    private const val REGIONAL_FEET = 28_000
    private const val LONG_HAUL_FEET = 36_000
    private const val DEGREES_PER_HOUR = 15.0
    private const val FULL_CIRCLE = 360.0

    fun at(flight: ActiveFlight, now: Instant): FlightTelemetry {
        val total = Duration.between(flight.startedAt, flight.plannedArrivalAt).toMinutes().coerceAtLeast(1L)
        val progress = ActiveFlightProgress.snapshot(flight, now).progress
        val ramp = (TARGET_RAMP_MINUTES / total).coerceIn(MIN_RAMP_FRACTION, MAX_RAMP_FRACTION)
        val cruiseFeet = cruiseAltitude(flight.distanceMiles)
        val level = level(progress, ramp)
        val altitude = (cruiseFeet * level).roundToInt()
        val stage = when {
            progress < ramp -> FlightStage.Climbing
            progress > 1.0 - ramp -> FlightStage.Descending
            else -> FlightStage.Cruising
        }
        return FlightTelemetry(
            stage = stage,
            altitudeFeet = altitude,
            groundSpeedMph = speed(progress, ramp),
            outsideTempF = temperature(altitude),
            headingDegrees = heading(flight, progress),
            originLocalTime = localTime(now, flight.origin),
            destinationLocalTime = localTime(now, flight.destination),
        )
    }

    private fun cruiseAltitude(distanceMiles: Double): Int = when {
        distanceMiles < SHORT_HOP_MILES -> SHORT_HOP_FEET
        distanceMiles < REGIONAL_MILES -> REGIONAL_FEET
        else -> LONG_HAUL_FEET
    }

    /** 0 on the ground, 1 at cruise: a smooth ramp up over the climb and back down over the descent. */
    private fun level(progress: Double, ramp: Double): Double {
        val edge = when {
            progress < ramp -> progress / ramp
            progress > 1.0 - ramp -> (1.0 - progress) / ramp
            else -> 1.0
        }.coerceIn(0.0, 1.0)
        return edge * edge * (3 - 2 * edge)
    }

    private fun speed(progress: Double, ramp: Double): Int {
        val climb = (progress / ramp).coerceIn(0.0, 1.0)
        val descent = ((1.0 - progress) / ramp).coerceIn(0.0, 1.0)
        return when {
            progress < ramp -> TAKEOFF_SPEED_MPH + ((CRUISE_SPEED_MPH - TAKEOFF_SPEED_MPH) * climb).roundToInt()
            progress > 1.0 - ramp -> APPROACH_SPEED_MPH + ((CRUISE_SPEED_MPH - APPROACH_SPEED_MPH) * descent).roundToInt()
            else -> CRUISE_SPEED_MPH
        }
    }

    private fun temperature(altitudeFeet: Int): Int =
        (SEA_LEVEL_TEMP_F - LAPSE_F_PER_THOUSAND_FEET * altitudeFeet / 1000.0).roundToInt().coerceAtLeast(COLDEST_TEMP_F)

    /** The direction the nose points: the bearing from the plane's position to the destination. */
    private fun heading(flight: ActiveFlight, progress: Double): Int {
        val here = FlightRouteInterpolation.interpolate(
            com.travelblock.app.domain.model.GeoPoint(flight.origin.latitude, flight.origin.longitude),
            com.travelblock.app.domain.model.GeoPoint(flight.destination.latitude, flight.destination.longitude),
            progress.coerceIn(0.0, 0.999),
        )
        val lat1 = Math.toRadians(here.latitude)
        val lat2 = Math.toRadians(flight.destination.latitude)
        val dLon = Math.toRadians(flight.destination.longitude - here.longitude)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return ((Math.toDegrees(atan2(y, x)) + FULL_CIRCLE) % FULL_CIRCLE).roundToInt() % FULL_CIRCLE.toInt()
    }

    /** Solar time at an airport (its longitude over 15 degrees per hour), written the way an arrivals board would. */
    private fun localTime(now: Instant, airport: Airport): String {
        val offsetMinutes = (airport.longitude / DEGREES_PER_HOUR * 60).roundToInt()
        val local = now.atOffset(java.time.ZoneOffset.UTC).plusMinutes(offsetMinutes.toLong())
        return local.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
    }
}
