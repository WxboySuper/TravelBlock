package com.travelblock.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_flight_session")
data class ActiveFlightSessionEntity(
    @PrimaryKey val sessionId: Int = SINGLE_SESSION_ID,
    val originCode: String,
    val originIcao: String,
    val originIata: String?,
    val originName: String,
    val originCity: String,
    val originStateOrRegion: String?,
    val originCountry: String,
    val originLatitude: Double,
    val originLongitude: Double,
    val originElevationFeet: Int?,
    val destinationCode: String,
    val destinationIcao: String,
    val destinationIata: String?,
    val destinationName: String,
    val destinationCity: String,
    val destinationStateOrRegion: String?,
    val destinationCountry: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val destinationElevationFeet: Int?,
    val durationMinutes: Int,
    val distanceMiles: Double,
    val seatId: String,
    val seatLabel: String,
    val seatTicketClass: String,
    val seatPointsCost: Int,
    val seatDescription: String,
    val seatIsAvailable: Boolean,
    val flightNumber: String,
    val startedAt: Long,
    val plannedArrivalAt: Long,
    val focusObjective: String,
    val focusTag: String,
    val status: String,
) {
    companion object {
        const val SINGLE_SESSION_ID = 1
    }
}
