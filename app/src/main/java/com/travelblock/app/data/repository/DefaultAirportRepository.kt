package com.travelblock.app.data.repository

import com.travelblock.app.domain.engine.AirportRouteEngine
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightRoute

class DefaultAirportRepository(
    private val airportProvider: () -> List<Airport>,
    private val routeEngine: AirportRouteEngine = AirportRouteEngine(),
) : AirportRepository {
    private val airports: List<Airport> by lazy {
        airportProvider()
            .filter { it.isUsableForTravelBlockRouting }
            .distinctBy { it.code }
    }

    override fun getAllAirports(): List<Airport> = airports

    override fun getAirportByCode(code: String): Airport? {
        val normalizedCode = code.trim().uppercase()
        if (normalizedCode.isEmpty()) return null

        return airports.firstOrNull { airport ->
            airport.code == normalizedCode ||
                airport.icao == normalizedCode ||
                airport.iata == normalizedCode
        }
    }

    override fun getReachableAirports(originCode: String, durationMinutes: Int): List<FlightRoute> {
        val origin = getAirportByCode(originCode) ?: return emptyList()
        return routeEngine.findReachableRoutes(
            origin = origin,
            airports = airports,
            durationMinutes = durationMinutes,
        )
    }
}
