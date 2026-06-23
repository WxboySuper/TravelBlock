package com.travelblock.app.data.repository

import com.travelblock.app.data.database.FlightLogDao
import com.travelblock.app.data.database.FlightLogEntity
import kotlinx.coroutines.flow.Flow

class DefaultFlightLogRepository(
    private val flightLogDao: FlightLogDao,
) : FlightLogRepository {
    override fun observeFlightLogs(): Flow<List<FlightLogEntity>> {
        return flightLogDao.observeFlightLogsNewestFirst()
    }

    override suspend fun getFlightLogs(): List<FlightLogEntity> {
        return flightLogDao.getFlightLogsNewestFirst()
    }

    override suspend fun getFlightById(id: String): FlightLogEntity? {
        return flightLogDao.getFlightLogById(id)
    }

    override suspend fun insertFlightLog(flightLog: FlightLogEntity) {
        flightLogDao.insertFlightLog(flightLog)
    }
}
