package com.travelblock.app.data.repository

import com.travelblock.app.data.database.ActiveFlightSessionDao
import com.travelblock.app.data.database.ActiveFlightSessionEntity
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Instant

class DefaultActiveFlightSessionRepository(
    private val activeFlightSessionDao: ActiveFlightSessionDao,
) : ActiveFlightSessionRepository {
    override suspend fun getActiveFlight(): ActiveFlight? {
        return activeFlightSessionDao.getActiveFlightSession()?.toDomainModel()
    }

    override suspend fun saveActiveFlight(activeFlight: ActiveFlight) {
        activeFlightSessionDao.upsertActiveFlightSession(activeFlight.toEntity())
    }

    override suspend fun clearActiveFlight() {
        activeFlightSessionDao.clearActiveFlightSession()
    }
}

private fun ActiveFlightSessionEntity.toDomainModel(): ActiveFlight {
    return ActiveFlight(
        origin = Airport(
            code = originCode,
            icao = originIcao,
            iata = originIata,
            name = originName,
            city = originCity,
            stateOrRegion = originStateOrRegion,
            country = originCountry,
            latitude = originLatitude,
            longitude = originLongitude,
            elevationFeet = originElevationFeet,
        ),
        destination = Airport(
            code = destinationCode,
            icao = destinationIcao,
            iata = destinationIata,
            name = destinationName,
            city = destinationCity,
            stateOrRegion = destinationStateOrRegion,
            country = destinationCountry,
            latitude = destinationLatitude,
            longitude = destinationLongitude,
            elevationFeet = destinationElevationFeet,
        ),
        durationMinutes = durationMinutes,
        distanceMiles = distanceMiles,
        seat = SeatOption(
            id = seatId,
            label = seatLabel,
            ticketClass = TicketClass.valueOf(seatTicketClass),
            pointsCost = seatPointsCost,
            description = seatDescription,
            isAvailable = seatIsAvailable,
        ),
        flightNumber = flightNumber,
        startedAt = Instant.ofEpochMilli(startedAt),
        plannedArrivalAt = Instant.ofEpochMilli(plannedArrivalAt),
        focusObjective = focusObjective,
        focusTag = focusTag,
        status = ActiveFlightStatus.valueOf(status),
    )
}

private fun ActiveFlight.toEntity(): ActiveFlightSessionEntity {
    return ActiveFlightSessionEntity(
        originCode = origin.code,
        originIcao = origin.icao,
        originIata = origin.iata,
        originName = origin.name,
        originCity = origin.city,
        originStateOrRegion = origin.stateOrRegion,
        originCountry = origin.country,
        originLatitude = origin.latitude,
        originLongitude = origin.longitude,
        originElevationFeet = origin.elevationFeet,
        destinationCode = destination.code,
        destinationIcao = destination.icao,
        destinationIata = destination.iata,
        destinationName = destination.name,
        destinationCity = destination.city,
        destinationStateOrRegion = destination.stateOrRegion,
        destinationCountry = destination.country,
        destinationLatitude = destination.latitude,
        destinationLongitude = destination.longitude,
        destinationElevationFeet = destination.elevationFeet,
        durationMinutes = durationMinutes,
        distanceMiles = distanceMiles,
        seatId = seat.id,
        seatLabel = seat.label,
        seatTicketClass = seat.ticketClass.name,
        seatPointsCost = seat.pointsCost,
        seatDescription = seat.description,
        seatIsAvailable = seat.isAvailable,
        flightNumber = flightNumber,
        startedAt = startedAt.toEpochMilli(),
        plannedArrivalAt = plannedArrivalAt.toEpochMilli(),
        focusObjective = focusObjective,
        focusTag = focusTag,
        status = status.name,
    )
}
