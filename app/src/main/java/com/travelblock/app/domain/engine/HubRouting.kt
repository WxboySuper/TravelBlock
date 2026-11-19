package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightItinerary
import com.travelblock.app.domain.model.ItinerarySchedule
import com.travelblock.app.domain.model.TripPreset
import com.travelblock.app.domain.model.isMajorHub

/** A short trip rerouted through a major hub: the hub, the two flight times, and what the detour adds. */
data class HubConnection(
    val hub: Airport,
    val firstLegMinutes: Int,
    val secondLegMinutes: Int,
    val layoverMinutes: Int,
    val directMinutes: Int,
) {
    val totalFocusMinutes: Int get() = firstLegMinutes + secondLegMinutes
    val extraMinutes: Int get() = totalFocusMinutes + layoverMinutes - directMinutes
}

/**
 * Local airports can be quick hops straight to each other, or, if you want it realistic, connect through the
 * nearest sensible major hub (small airport, hub, small airport) at the cost of the detour and a layover.
 */
object HubRouting {
    private const val MAX_OPTIONS = 3
    private const val MAX_DETOUR_FACTOR = 2.2
    private const val MAX_DETOUR_EXTRA_MILES = 250.0
    private val engine = AirportRouteEngine()

    /** Null when both ends are major airports, or no hub makes a sensible detour. */
    fun connect(origin: Airport, destination: Airport, airports: List<Airport>): HubConnection? =
        connections(origin, destination, airports).firstOrNull()

    /** The sensible hubs for this trip, least detour first, so the traveller can pick one. */
    fun connections(origin: Airport, destination: Airport, airports: List<Airport>): List<HubConnection> {
        if (origin.code == destination.code) return emptyList()
        // Two major airports connect directly; a small airport at either end connects through a hub first.
        val bothMajor = origin.isMajorHub && destination.isMajorHub
        if (bothMajor) return emptyList()
        val direct = engine.distanceMiles(origin, destination)
        val limit = direct * MAX_DETOUR_FACTOR + MAX_DETOUR_EXTRA_MILES
        return airports.asSequence()
            .filter { it.isMajorHub && it.isUsableForTravelBlockRouting }
            .filter { it.code != origin.code && it.code != destination.code }
            .map { it to engine.distanceMiles(origin, it) + engine.distanceMiles(it, destination) }
            .filter { it.second <= limit }
            .sortedBy { it.second }
            .take(MAX_OPTIONS)
            .map { (hub, _) -> connectionVia(origin, destination, hub, direct) }
            .toList()
    }

    private fun connectionVia(origin: Airport, destination: Airport, hub: Airport, direct: Double) = HubConnection(
        hub = hub,
        firstLegMinutes = engine.estimatedDurationMinutes(engine.distanceMiles(origin, hub)),
        secondLegMinutes = engine.estimatedDurationMinutes(engine.distanceMiles(hub, destination)),
        layoverMinutes = FlightItinerary.STANDARD_LAYOVER_MINUTES,
        directMinutes = engine.estimatedDurationMinutes(direct),
    )

    fun itinerary(origin: Airport, destination: Airport, connection: HubConnection): FlightItinerary = FlightItinerary(
        preset = TripPreset("hub", "Via ${connection.hub.displayCode}", 2),
        airports = listOf(origin, connection.hub, destination),
        schedule = ItinerarySchedule(
            focusMinutes = connection.totalFocusMinutes,
            legDurationsMinutes = listOf(connection.firstLegMinutes, connection.secondLegMinutes),
            breakDurationsMinutes = listOf(connection.layoverMinutes),
        ),
    )
}
