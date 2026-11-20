package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightItinerary
import com.travelblock.app.domain.model.ItinerarySchedule
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.domain.model.TripPreset
import com.travelblock.app.domain.model.isMajorHub
import com.travelblock.app.domain.model.isSecondaryHub
import kotlin.math.abs

object ItineraryPlanner {
    private const val SECONDARY_HUB_PENALTY_MILES = 400.0
    private const val MIN_LEGS_FOR_ORIGIN_HUB = 3
    private const val MIN_LEGS_FOR_DESTINATION_HUB = 4

    /**
     * A stop closer than this to the start or the end of the trip is the same city, not a connection: the leg to it
     * would still be given a whole focus block and a ten mile hop would take forty minutes.
     */
    private const val MIN_STOP_SEPARATION_MILES = 60.0

    fun plan(request: ItineraryRequest): FlightItinerary? {
        val (origin, destination, airports, trip) = request
        val preset = trip.preset
        if (origin.code == destination.code || trip.focusMinutes <= 0) return null
        val route = mutableListOf<Airport>()
        val context = RouteContext(origin, destination)
        for (index in 1 until preset.legCount) {
            val target = stopTarget(index, preset.legCount, context)
            val excluded = route.mapTo(mutableSetOf()) { it.code }
            val stop = (if (preset.legCount == 2) HubRouting.connect(origin, destination, airports)?.hub else null)
                ?: chooseStop(target, airports, excluded, context)
                ?: chooseRepeatedStop(target, airports, context)
                ?: return null
            route += stop
        }
        val airportsInOrder = listOf(origin) + route + destination
        // Every flight takes as long as its distance says, whatever focus block the trip was sized from.
        val legMinutes = LegTimes.forStops(airportsInOrder)
        return FlightItinerary(
            preset = preset,
            airports = airportsInOrder,
            schedule = ItinerarySchedule(
                focusMinutes = legMinutes.sum(),
                legDurationsMinutes = legMinutes,
                breakDurationsMinutes = trip.breakDurationsMinutes,
            ),
        )
    }

    /**
     * Where the next stop should be near. Evenly spaced along the route, except that a small airport first
     * connects through its own regional hub: the first stop sits by a small origin, the last by a small destination.
     */
    private fun stopTarget(index: Int, legCount: Int, context: RouteContext): GeoPoint {
        val (origin, destination) = context
        val nearOrigin = index == 1 && legCount >= MIN_LEGS_FOR_ORIGIN_HUB && !origin.isMajorHub
        val nearDestination = index == legCount - 1 && legCount >= MIN_LEGS_FOR_DESTINATION_HUB && !destination.isMajorHub
        return when {
            nearOrigin -> origin.toPoint()
            nearDestination -> destination.toPoint()
            else -> FlightRouteInterpolation.interpolate(origin.toPoint(), destination.toPoint(), index.toDouble() / legCount)
        }
    }

    private fun chooseRepeatedStop(target: GeoPoint, airports: List<Airport>, context: RouteContext): Airport? =
        airports.asSequence()
            .filter(Airport::isUsableForTravelBlockRouting)
            .filterNot { it.code == context.origin.code || it.code == context.destination.code }
            .minByOrNull { stopScore(it, target, context) }

    private fun chooseStop(
        target: GeoPoint,
        airports: List<Airport>,
        excludedCodes: Set<String>,
        context: RouteContext,
    ): Airport? {
        val candidates = airports.asSequence()
            .filter(Airport::isUsableForTravelBlockRouting)
            .filterNot { it.code == context.origin.code || it.code == context.destination.code || it.code in excludedCodes }
            .toList()
        val apart = candidates.filter { isFarFromEnds(it, context) }.ifEmpty { candidates }
        // Connections happen at major hubs; a small airport is only ever the start or the end of a trip.
        val pool = apart.filter { it.isMajorHub }.ifEmpty { apart }
        return pool.minByOrNull { airport -> stopScore(airport, target, context) }
    }

    private fun isFarFromEnds(airport: Airport, context: RouteContext): Boolean =
        listOf(context.origin, context.destination).all { end ->
            AirportRouteEngine.distanceMiles(end.latitude, end.longitude, airport.latitude, airport.longitude) >= MIN_STOP_SEPARATION_MILES
        }

    private fun stopScore(
        airport: Airport,
        target: GeoPoint,
        context: RouteContext,
    ): Double {
        val positionError = distance(airport, target)
        val fromOrigin = AirportRouteEngine.distanceMiles(
            context.origin.latitude, context.origin.longitude, airport.latitude, airport.longitude,
        )
        val toDestination = AirportRouteEngine.distanceMiles(
            airport.latitude, airport.longitude, context.destination.latitude, context.destination.longitude,
        )
        val secondary = if (airport.isSecondaryHub) SECONDARY_HUB_PENALTY_MILES else 0.0
        return positionError + abs(fromOrigin - toDestination) * 0.08 + preferredHubPenalty(airport, context) + secondary
    }

    private fun preferredHubPenalty(airport: Airport, context: RouteContext): Double {
        if (AirportRouteEngine.distanceMiles(
                context.origin.latitude, context.origin.longitude,
                context.destination.latitude, context.destination.longitude,
            ) < 5_000
        ) return 0.0
        val hubCode = airport.iata?.uppercase().orEmpty()
        return if (hubCode in preferredHubCodes(context.origin, context.destination)) -1_500.0 else 0.0
    }

    private fun preferredHubCodes(origin: Airport, destination: Airport): Set<String> =
        if (origin.country.equals("US", ignoreCase = true) && !destination.country.equals("US", ignoreCase = true)) {
            setOf("KEF", "LHR", "NRT", "HNL", "ANC", "DOH", "DXB")
        } else {
            setOf("LHR", "CDG", "FRA", "DOH", "DXB", "HKG", "NRT", "SIN", "HNL", "ANC")
        }

    private fun distance(airport: Airport, point: GeoPoint): Double = AirportRouteEngine.distanceMiles(
        airport.latitude,
        airport.longitude,
        point.latitude,
        point.longitude,
    )

    private fun Airport.toPoint() = GeoPoint(latitude, longitude)

    private data class RouteContext(val origin: Airport, val destination: Airport)
}

data class ItineraryRequest(
    val origin: Airport,
    val destination: Airport,
    val airports: List<Airport>,
    val trip: ItineraryTrip,
)

data class ItineraryTrip(
    val preset: TripPreset,
    val focusMinutes: Int,
    val legDurationsMinutes: List<Int> = emptyList(),
    val breakDurationsMinutes: List<Int> = emptyList(),
)
