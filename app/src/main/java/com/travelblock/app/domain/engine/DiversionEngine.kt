package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport

data class DiversionAirportResult(
    val airport: Airport,
    val distanceMiles: Double,
)

object DiversionEngine {
    fun findClosestDiversionAirport(
        flight: ActiveFlight,
        airports: List<Airport>,
        progress: Double,
    ): DiversionAirportResult? {
        if (airports.isEmpty()) return null

        val position = interpolatePosition(flight, progress.coerceIn(0.0, 1.0))
        return airports.asSequence()
            .filter { airport -> airport.code != flight.destination.code }
            .mapNotNull { airport ->
                val distance = AirportRouteEngine.distanceMiles(
                    fromLatitude = position.latitude,
                    fromLongitude = position.longitude,
                    toLatitude = airport.latitude,
                    toLongitude = airport.longitude,
                )
                if (!distance.isFinite()) {
                    null
                } else {
                    DiversionAirportResult(airport = airport, distanceMiles = distance)
                }
            }
            .minWithOrNull(
                compareBy<DiversionAirportResult> { it.distanceMiles }
                    .thenByDescending { it.airport.hasCompleteCodes }
                    .thenBy { it.airport.code },
            )
    }

    private fun interpolatePosition(
        flight: ActiveFlight,
        progress: Double,
    ): FlightPosition {
        return FlightPosition(
            latitude = flight.origin.latitude + (flight.destination.latitude - flight.origin.latitude) * progress,
            longitude = flight.origin.longitude + (flight.destination.longitude - flight.origin.longitude) * progress,
        )
    }

    private val Airport.hasCompleteCodes: Boolean
        get() = icao.isNotBlank() && !iata.isNullOrBlank()

    private data class FlightPosition(
        val latitude: Double,
        val longitude: Double,
    )
}
