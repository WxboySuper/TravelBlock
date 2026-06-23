package com.travelblock.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlightLog(flightLog: FlightLogEntity)

    @Query("SELECT * FROM flight_logs ORDER BY endedAt DESC")
    fun observeFlightLogsNewestFirst(): Flow<List<FlightLogEntity>>

    @Query("SELECT * FROM flight_logs ORDER BY endedAt DESC")
    suspend fun getFlightLogsNewestFirst(): List<FlightLogEntity>

    @Query("SELECT * FROM flight_logs WHERE id = :id LIMIT 1")
    suspend fun getFlightLogById(id: String): FlightLogEntity?
}
