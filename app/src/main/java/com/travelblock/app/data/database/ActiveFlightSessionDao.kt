package com.travelblock.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActiveFlightSessionDao {
    @Query("SELECT * FROM active_flight_session WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getActiveFlightSession(sessionId: Int = ActiveFlightSessionEntity.SINGLE_SESSION_ID): ActiveFlightSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActiveFlightSession(session: ActiveFlightSessionEntity)

    @Query("DELETE FROM active_flight_session")
    suspend fun clearActiveFlightSession()
}
