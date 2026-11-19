package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightItinerary
import com.travelblock.app.domain.model.ItinerarySchedule
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.domain.model.TripPreset
import kotlin.math.abs

object ItineraryPlanner {
    fun plan(request: ItineraryRequest): FlightItinerary? {
        val (origin, destination, airports, trip) = request
        val preset = trip.preset
        if (origin.code == destination.code || trip.focusMinutes <= 0) return null
        val route = mutableListOf<Airport>()
        val context = RouteContext(origin, destination)
        for (index in 1 until preset.legCount) {
            val target = FlightRouteInterpolation.interpolate(
                origin.toPoint(), destination.toPoint(), index.toDouble() / preset.legCount,
            )
            val excluded = route.mapTo(mutableSetOf()) { it.code }
            val stop = chooseStop(target, airports, excluded, context)
                ?: chooseRepeatedStop(target, airports, context)
                ?: return null
            route += stop
        }
        return FlightItinerary(
            preset = preset,
            airports = listOf(origin) + route + destination,
            schedule = ItinerarySchedule(
                focusMinutes = trip.focusMinutes,
                legDurationsMinutes = trip.legDurationsMinutes.ifEmpty { List(preset.legCount) { trip.focusMinutes } },
                breakDurationsMinutes = trip.breakDurationsMinutes,
            ),
        )
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
        return airports.asSequence()
            .filter(Airport::isUsableForTravelBlockRouting)
            .filterNot { it.code == context.origin.code || it.code == context.destination.code || it.code in excludedCodes }
            .minByOrNull { airport -> stopScore(airport, target, context) }
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
        return positionError + abs(fromOrigin - toDestination) * 0.08 + preferredHubPenalty(airport, context)
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
