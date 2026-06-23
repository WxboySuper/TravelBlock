package com.travelblock.app.data.repository

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightRoute

interface AirportRepository {
    fun getAllAirports(): List<Airport>
    fun getAirportByCode(code: String): Airport?
    fun getReachableAirports(originCode: String, durationMinutes: Int): List<FlightRoute>
}

