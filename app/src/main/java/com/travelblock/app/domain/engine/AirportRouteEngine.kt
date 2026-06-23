package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightRoute
import com.travelblock.app.domain.model.RouteAvailabilityResult
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class AirportRouteEngine(
    private val config: RouteEngineConfig = RouteEngineConfig,
) {
    fun distanceMiles(from: Airport, to: Airport): Double {
        return greatCircleDistanceMiles(
            fromLatitude = from.latitude,
            fromLongitude = from.longitude,
            toLatitude = to.latitude,
            toLongitude = to.longitude,
        )
    }

    fun findReachableRoutes(
        origin: Airport?,
        airports: List<Airport>,
        durationMinutes: Int,
    ): List<FlightRoute> {
        if (origin == null || durationMinutes <= 0 || airports.isEmpty()) return emptyList()

        val maxFlightTime = durationMinutes

        return airports.asSequence()
            .filter { it.isUsableForTravelBlockRouting }
            .filter { it.code != origin.code }
            .mapNotNull { destination ->
                val distance = distanceMiles(origin, destination)
                val estimatedDuration = estimatedDurationMinutes(distance)
                if (!distance.isFinite() || estimatedDuration > maxFlightTime) {
                    null
                } else {
                    val targetDistance = targetDistanceMiles(maxFlightTime)
                    FlightRoute(
                        origin = origin,
                        destination = destination,
                        distanceMiles = distance,
                        targetDistanceMiles = targetDistance,
                        selectedDurationMinutes = maxFlightTime,
                        estimatedDurationMinutes = estimatedDuration,
                        availability = availabilityFor(destination, estimatedDuration, maxFlightTime),
                    )
                }
            }
            .sortedWith(compareByDescending<FlightRoute> { destinationPriority(it.destination) }
                .thenBy { sortPriority(it.availability) }
                .thenBy { abs(it.selectedDurationMinutes - it.estimatedDurationMinutes) }
                .thenBy { it.distanceMiles })
            .toList()
    }

    fun targetDistanceMiles(durationMinutes: Int): Double {
        if (durationMinutes <= 0) return 0.0
        return durationMinutes / 60.0 * config.SimulatedCruiseSpeedMph
    }

    fun estimatedDurationMinutes(distanceMiles: Double): Int {
        val cruiseSpeed = if (distanceMiles < 120.0) {
            config.ShortRouteCruiseSpeedMph
        } else {
            config.SimulatedCruiseSpeedMph
        }
        val cruiseMinutes = distanceMiles / cruiseSpeed * 60.0
        return cruiseMinutes.roundToInt().coerceAtLeast(config.MinimumFlightTimeMinutes)
    }

    private fun availabilityFor(
        airport: Airport,
        estimatedDurationMinutes: Int,
        maxDurationMinutes: Int,
    ): RouteAvailabilityResult {
        val name = airport.name.lowercase()
        return when {
            config.MajorAirportNameSignals.any { it in name } -> RouteAvailabilityResult.BestMatch
            estimatedDurationMinutes >= maxDurationMinutes * config.LongStretchTimeThreshold -> RouteAvailabilityResult.LongStretch
            else -> RouteAvailabilityResult.ShortHop
        }
    }

    private fun sortPriority(result: RouteAvailabilityResult): Int {
        return when (result) {
            RouteAvailabilityResult.BestMatch -> 0
            RouteAvailabilityResult.ShortHop -> 1
            RouteAvailabilityResult.LongStretch -> 2
        }
    }

    private fun destinationPriority(airport: Airport): Double {
        val name = airport.name.lowercase()
        val airportSizeScore = when {
            config.MajorAirportNameSignals.any { it in name } -> config.MajorAirportPriorityBoost
            config.RegionalAirportNameSignals.any { it in name } -> config.RegionalAirportPriorityBoost
            config.NamedAirportSignals.any { it in name } -> config.NamedAirportPriorityBoost
            else -> 0.0
        }
        val codeScore = if (airport.icao.isNotBlank() && !airport.iata.isNullOrBlank()) {
            config.CompleteCodePriorityBoost
        } else {
            0.0
        }
        return airportSizeScore + codeScore
    }

    private fun greatCircleDistanceMiles(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
    ): Double {
        val earthRadiusMiles = 3_958.8
        val fromLat = Math.toRadians(fromLatitude)
        val toLat = Math.toRadians(toLatitude)
        val deltaLat = Math.toRadians(toLatitude - fromLatitude)
        val deltaLon = Math.toRadians(toLongitude - fromLongitude)

        val haversine = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(fromLat) * cos(toLat) * sin(deltaLon / 2) * sin(deltaLon / 2)

        return 2 * earthRadiusMiles * asin(sqrt(haversine))
    }

    companion object {
        fun distanceMiles(
            fromLatitude: Double,
            fromLongitude: Double,
            toLatitude: Double,
            toLongitude: Double,
        ): Double {
            val earthRadiusMiles = 3_958.8
            val fromLat = Math.toRadians(fromLatitude)
            val toLat = Math.toRadians(toLatitude)
            val deltaLat = Math.toRadians(toLatitude - fromLatitude)
            val deltaLon = Math.toRadians(toLongitude - fromLongitude)

            val haversine = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(fromLat) * cos(toLat) * sin(deltaLon / 2) * sin(deltaLon / 2)

            return 2 * earthRadiusMiles * asin(sqrt(haversine))
        }
    }
}
