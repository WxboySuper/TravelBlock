package com.travelblock.app.data.repository

import com.travelblock.app.data.database.FlightLogEntity
import kotlinx.coroutines.flow.Flow

interface FlightLogRepository {
    fun observeFlightLogs(): Flow<List<FlightLogEntity>>
    suspend fun getFlightLogs(): List<FlightLogEntity>
    suspend fun getFlightById(id: String): FlightLogEntity?
    suspend fun insertFlightLog(flightLog: FlightLogEntity)
}
