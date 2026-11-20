package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveItinerarySchedule
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.Airport
import java.time.Instant
import com.travelblock.app.domain.engine.SessionPlanCalculator

private fun hubItinerary(pass: BoardingPass, hubCode: String, airports: List<Airport>) =
    HubRouting.connections(pass.origin, pass.destination, airports)
        .firstOrNull { it.hub.code == hubCode }
        ?.let { HubRouting.itinerary(pass.origin, pass.destination, it) }

fun createInitialFlight(
    pass: BoardingPass,
    airports: List<Airport>,
    startedAt: Instant,
): ActiveFlight {
    val singleFlight = pass.toActiveFlight(startedAt)
    val preset = pass.tripPreset ?: return singleFlight
    val breakdown = pass.sessionPlan?.let(SessionPlanCalculator::calculate)
    val itinerary = (pass.viaHubCode?.let { hubItinerary(pass, it, airports) }) ?: ItineraryPlanner.plan(
        ItineraryRequest(
            origin = pass.origin,
            destination = pass.destination,
            airports = airports,
            trip = ItineraryTrip(
                preset = preset,
                focusMinutes = pass.focusDurationMinutes,
                legDurationsMinutes = breakdown?.legDurationsMinutes.orEmpty(),
                breakDurationsMinutes = breakdown?.breakDurationsMinutes.orEmpty(),
            ),
        ),
    ) ?: return singleFlight
    val first = itinerary.airports.take(2)
    val engine = AirportRouteEngine()
    val totalDistance = itinerary.airports.zipWithNext().sumOf { (from, to) ->
        engine.distanceMiles(from, to)
    }
    val firstLegMinutes = itinerary.legDurationsMinutes.first()
    return singleFlight.copy(
        origin = first[0],
        destination = first[1],
        durationMinutes = firstLegMinutes,
        plannedArrivalAt = startedAt.plusSeconds(firstLegMinutes * 60L),
        distanceMiles = engine.distanceMiles(first[0], first[1]),
        itineraryCodes = itinerary.airports.joinToString(",") { it.code },
        itineraryLabel = itinerary.preset.label,
        itinerarySchedule = ActiveItinerarySchedule(
            legDurationsMinutes = itinerary.legDurationsMinutes,
            breakDurationsMinutes = itinerary.breakDurationsMinutes,
            longBreakMinutes = pass.sessionPlan?.breaks?.longMinutes,
            longBreakEvery = pass.sessionPlan?.breaks?.longEveryBlocks,
        ),
        itineraryLegIndex = 0,
        itineraryLegCount = itinerary.legDurationsMinutes.size,
        itineraryTaskPerLeg = pass.taskPerLeg,
        itineraryDistanceMiles = totalDistance,
    )
}
