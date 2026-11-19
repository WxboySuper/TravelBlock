package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.Airport
import java.time.Instant

fun createInitialFlight(
    pass: BoardingPass,
    airports: List<Airport>,
    startedAt: Instant,
): ActiveFlight {
    val singleFlight = pass.toActiveFlight(startedAt)
    val preset = pass.tripPreset ?: return singleFlight
    val itinerary = ItineraryPlanner.plan(
        ItineraryRequest(pass.origin, pass.destination, preset, airports, pass.focusDurationMinutes),
    ) ?: return singleFlight
    val first = itinerary.airports.take(2)
    val engine = AirportRouteEngine()
    val totalDistance = itinerary.airports.zipWithNext().sumOf { (from, to) ->
        engine.distanceMiles(from, to)
    }
    return singleFlight.copy(
        origin = first[0],
        destination = first[1],
        distanceMiles = engine.distanceMiles(first[0], first[1]),
        itineraryCodes = itinerary.airports.joinToString(",") { it.code },
        itineraryLabel = preset.label,
        itineraryLegIndex = 0,
        itineraryLegCount = preset.legCount,
        itineraryDistanceMiles = totalDistance,
    )
}
