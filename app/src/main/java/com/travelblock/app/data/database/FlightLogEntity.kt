package com.travelblock.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flight_logs")
data class FlightLogEntity(
    @PrimaryKey val id: String,
    val flightNumber: String,
    val originCode: String,
    val destinationCode: String,
    val durationMinutes: Int,
    val distanceMiles: Double,
    val status: String,
    val startedAt: Long,
    val endedAt: Long,
    val seat: String,
    val pointsEarned: Int,
    val focusObjective: String = "",
    val focusTag: String = "",
)
