package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightRoute
import com.travelblock.app.domain.model.TripPreset
import com.travelblock.app.domain.model.isMajorHub
import kotlin.math.abs
import kotlin.math.atan2

/** What the traveller asks the planner for: how long they want to focus, and how realistic to make it. */
data class TripWish(val minutes: Int, val connectThroughHubs: Boolean = true, val majorAirportsOnly: Boolean = true)

/** One offer from the planner: where it goes, every airport on the way (hubs included) and how long it focuses. */
data class TripSuggestion(
    val route: FlightRoute,
    val hub: HubConnection?,
    val focusMinutes: Int,
    val stops: List<Airport>,
)

/**
 * Builds a few trips to choose from for a focus time: the best fits in different directions, each already
 * connected through the hubs a real trip would use. The traveller picks; nothing is chosen for them.
 */
object TripSuggester {
    private const val MAX_CANDIDATES = 120
    private const val MAX_OPTIONS = 3
    private const val QUADRANT_DEGREES = 90
    private val engine = AirportRouteEngine()

    fun options(origin: Airport, airports: List<Airport>, wish: TripWish): List<TripSuggestion> {
        val hubs = airports.filter { it.isMajorHub && it.isUsableForTravelBlockRouting }
        val fits = engine.findReachableRoutes(origin, airports, wish.minutes)
            .filter { !wish.majorAirportsOnly || it.destination.isMajorHub }
            .take(MAX_CANDIDATES)
            .map { route -> Fit(suggestionFor(route, hubs, airports, wish), wish.minutes) }
        // The best fit in each direction, then the best few overall, so the options really differ.
        return fits.groupBy { quadrant(origin, it.suggestion.route.destination) }.values
            .map { inDirection -> inDirection.minBy(Fit::gap) }
            .sortedBy(Fit::gap)
            .take(MAX_OPTIONS)
            .map(Fit::suggestion)
    }

    /** How far a suggestion is from the time asked for: its flying time, hub detour included. */
    private class Fit(val suggestion: TripSuggestion, private val wanted: Int) {
        val gap: Int get() = abs(wanted - (suggestion.hub?.totalFocusMinutes ?: suggestion.route.estimatedDurationMinutes))
    }

    private fun suggestionFor(route: FlightRoute, hubs: List<Airport>, airports: List<Airport>, wish: TripWish): TripSuggestion {
        if (wish.minutes > AutoSessionPlan.SINGLE_FLIGHT_MAX_MINUTES) return longHaul(route, airports, wish)
        val hub = if (wish.connectThroughHubs) HubRouting.connect(route.origin, route.destination, hubs) else null
        val stops = listOfNotNull(route.origin, hub?.hub, route.destination)
        return TripSuggestion(route, hub, hub?.totalFocusMinutes ?: route.estimatedDurationMinutes, stops)
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
        return TripSuggestion(route, null, wish.minutes, stops)
    }

    private fun quadrant(from: Airport, to: Airport): Int {
        val bearing = Math.toDegrees(atan2(to.longitude - from.longitude, to.latitude - from.latitude))
        return Math.floorMod(Math.floor(bearing / QUADRANT_DEGREES).toInt(), 360 / QUADRANT_DEGREES)
    }
}
