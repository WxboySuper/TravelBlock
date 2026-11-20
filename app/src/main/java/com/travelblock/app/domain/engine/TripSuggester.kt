package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightRoute
import com.travelblock.app.domain.model.TripPreset
import com.travelblock.app.domain.model.isMajorHub
import com.travelblock.app.domain.model.isSecondaryHub
import kotlin.math.abs
import kotlin.math.atan2

/** What the traveller asks the planner for: how long they want to focus, and how realistic to make it. */
data class TripWish(
    val minutes: Int,
    val connectThroughHubs: Boolean = true,
    val majorAirportsOnly: Boolean = true,
    /** Airports (any code form) the traveller has already landed at; the planner prefers somewhere new. */
    val visited: Set<String> = emptySet(),
    /** True for a "land by" deadline: a hub layover then eats into the time available. */
    val layoversCountAgainstTime: Boolean = false,
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
    private const val SECONDARY_AIRPORT_PENALTY_MINUTES = 4
    private val engine = AirportRouteEngine()

    fun options(origin: Airport, airports: List<Airport>, wish: TripWish): List<TripSuggestion> {
        val hubs = airports.filter { it.isMajorHub && it.isUsableForTravelBlockRouting }
        // Closest to the time asked for first, whatever the airport size: the size filter is the traveller's to set.
        val fits = engine.findReachableRoutes(origin, airports, wish.minutes)
            .filter { !wish.majorAirportsOnly || it.destination.isMajorHub }
            .sortedBy { abs(wish.minutes - it.estimatedDurationMinutes) }
            .take(MAX_CANDIDATES)
            .map { route -> Fit(suggestionFor(route, hubs, airports, wish), wish) }
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
    private class Fit(val suggestion: TripSuggestion, private val wish: TripWish) {
        val score: Int
            get() {
                val layover = suggestion.hub?.layoverMinutes?.takeIf { wish.layoversCountAgainstTime } ?: 0
                val gap = abs(wish.minutes - (suggestion.hub?.totalFocusMinutes?.plus(layover) ?: suggestion.focusMinutes))
                val visitedPenalty = if (suggestion.isNew) 0 else VISITED_PENALTY_MINUTES
                val destination = suggestion.route.destination
                val sizePenalty = when {
                    destination.isSecondaryHub -> SECONDARY_AIRPORT_PENALTY_MINUTES
                    destination.isMajorHub -> 0
                    else -> SMALL_AIRPORT_PENALTY_MINUTES
                }
                return gap + visitedPenalty + sizePenalty
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
        val itinerary = plan?.let {
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
            )
        }
        val stops = itinerary?.airports ?: listOf(route.origin, route.destination)
        // The time shown is the time the flights really take, which is what the distances add up to.
        return TripSuggestion(route, null, itinerary?.totalFocusMinutes ?: route.estimatedDurationMinutes, stops, isNewTo(route.destination, wish))
    }

    private fun isNewTo(destination: Airport, wish: TripWish): Boolean =
        listOfNotNull(destination.code, destination.icao, destination.iata).none { it in wish.visited }

    private fun quadrant(from: Airport, to: Airport): Int {
        val bearing = Math.toDegrees(atan2(to.longitude - from.longitude, to.latitude - from.latitude))
        return Math.floorMod(Math.floor(bearing / QUADRANT_DEGREES).toInt(), 360 / QUADRANT_DEGREES)
    }
}
