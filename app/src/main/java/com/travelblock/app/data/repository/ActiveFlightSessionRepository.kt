package com.travelblock.app.data.repository

import com.travelblock.app.domain.model.ActiveFlight

interface ActiveFlightSessionRepository {
    suspend fun getActiveFlight(): ActiveFlight?
    suspend fun saveActiveFlight(activeFlight: ActiveFlight)
    suspend fun clearActiveFlight()
}
