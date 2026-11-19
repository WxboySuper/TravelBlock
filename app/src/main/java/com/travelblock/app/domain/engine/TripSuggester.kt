package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightRoute
import com.travelblock.app.domain.model.TripPreset
import com.travelblock.app.domain.model.isMajorHub
import kotlin.math.abs
import kotlin.math.atan2

/** What the traveller asks the planner for: how long they want to focus, and how realistic to make it. */
data class TripWish(
    val minutes: Int,
    val connectThroughHubs: Boolean = true,
    val majorAirportsOnly: Boolean = true,
    /** Airports (any code form) the traveller has already landed at; the planner prefers somewhere new. */
    val visited: Set<String> = emptySet(),
)

/** One offer from the planner: where it goes, every airport on the way (hubs included) and how long it focuses. */
data class TripSuggestion(
    val route: FlightRoute,
    val hub: HubConnection?,
    val focusMinutes: Int,
    val stops: List<Airport>,
    /** True when the destination is somewhere the traveller has not landed before. */
    val isNew: Boolean = false,
)

/**
 * Builds a few trips to choose from for a focus time: the best fits in different directions, each already
 * connected through the hubs a real trip would use. The traveller picks; nothing is chosen for them.
 */
object TripSuggester {
    private const val MAX_CANDIDATES = 120
    private const val MAX_OPTIONS = 3
    private const val QUADRANT_DEGREES = 90
    private const val VISITED_PENALTY_MINUTES = 30
    private const val SMALL_AIRPORT_PENALTY_MINUTES = 8
    private val engine = AirportRouteEngine()

    fun options(origin: Airport, airports: List<Airport>, wish: TripWish): List<TripSuggestion> {
        val hubs = airports.filter { it.isMajorHub && it.isUsableForTravelBlockRouting }
        // Closest to the time asked for first, whatever the airport size: the size filter is the traveller's to set.
        val fits = engine.findReachableRoutes(origin, airports, wish.minutes)
            .filter { !wish.majorAirportsOnly || it.destination.isMajorHub }
            .sortedBy { abs(wish.minutes - it.estimatedDurationMinutes) }
            .take(MAX_CANDIDATES)
            .map { route -> Fit(suggestionFor(route, hubs, airports, wish), wish.minutes) }
        // The best fit in each direction, then the best few overall, so the options really differ.
        return fits.groupBy { quadrant(origin, it.suggestion.route.destination) }.values
            .map { inDirection -> inDirection.minBy(Fit::score) }
            .sortedBy(Fit::score)
            .take(MAX_OPTIONS)
            .map(Fit::suggestion)
    }

    /**
     * How well a suggestion suits: minutes away from the time asked for (hub detour included), plus a nudge
     * against somewhere already visited and a smaller one against small airports.
     */
    private class Fit(val suggestion: TripSuggestion, private val wanted: Int) {
        val score: Int
            get() {
                val gap = abs(wanted - (suggestion.hub?.totalFocusMinutes ?: suggestion.route.estimatedDurationMinutes))
                val visitedPenalty = if (suggestion.isNew) 0 else VISITED_PENALTY_MINUTES
                val smallPenalty = if (suggestion.route.destination.isMajorHub) 0 else SMALL_AIRPORT_PENALTY_MINUTES
                return gap + visitedPenalty + smallPenalty
            }
    }

    private fun suggestionFor(route: FlightRoute, hubs: List<Airport>, airports: List<Airport>, wish: TripWish): TripSuggestion {
        if (wish.minutes > AutoSessionPlan.SINGLE_FLIGHT_MAX_MINUTES) return longHaul(route, airports, wish)
        val hub = if (wish.connectThroughHubs) HubRouting.connect(route.origin, route.destination, hubs) else null
        val stops = listOfNotNull(route.origin, hub?.hub, route.destination)
        return TripSuggestion(route, hub, hub?.totalFocusMinutes ?: route.estimatedDurationMinutes, stops, isNewTo(route.destination, wish))
    }

    /** A long trip is flown in blocks with breaks; the planner puts its connections at hubs, so ask it where. */
    private fun longHaul(route: FlightRoute, airports: List<Airport>, wish: TripWish): TripSuggestion {
        val plan = AutoSessionPlan.forTotal(wish.minutes)?.let(SessionPlanCalculator::calculate)
        val stops = plan?.let {
            ItineraryPlanner.plan(
                ItineraryRequest(
                    origin = route.origin,
                    destination = route.destination,
                    airports = airports,
                    trip = ItineraryTrip(
                        preset = TripPreset("planned", "Planned session", it.legCount),
                        focusMinutes = wish.minutes,
                        legDurationsMinutes = it.legDurationsMinutes,
                        breakDurationsMinutes = it.breakDurationsMinutes,
                    ),
                ),
            )?.airports
        } ?: listOf(route.origin, route.destination)
        return TripSuggestion(route, null, wish.minutes, stops, isNewTo(route.destination, wish))
    }

    private fun isNewTo(destination: Airport, wish: TripWish): Boolean =
        listOfNotNull(destination.code, destination.icao, destination.iata).none { it in wish.visited }

    private fun quadrant(from: Airport, to: Airport): Int {
        val bearing = Math.toDegrees(atan2(to.longitude - from.longitude, to.latitude - from.latitude))
        return Math.floorMod(Math.floor(bearing / QUADRANT_DEGREES).toInt(), 360 / QUADRANT_DEGREES)
    }
}
