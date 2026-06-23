package com.travelblock.app.data.repository

import com.travelblock.app.data.database.ActiveFlightSessionDao
import com.travelblock.app.data.database.ActiveFlightSessionEntity
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultActiveFlightSessionRepositoryTest {
    @Test
    fun saveAndLoadRoundTripsActiveFlight() = runTest {
        val dao = FakeActiveFlightSessionDao()
        val repository = DefaultActiveFlightSessionRepository(dao)

        repository.saveActiveFlight(activeFlight)
        val restored = repository.getActiveFlight()

        assertEquals(activeFlight, restored)
    }

    @Test
    fun clearRemovesSavedSession() = runTest {
        val dao = FakeActiveFlightSessionDao()
        val repository = DefaultActiveFlightSessionRepository(dao)
        repository.saveActiveFlight(activeFlight)

        repository.clearActiveFlight()

        assertNull(repository.getActiveFlight())
    }

    private class FakeActiveFlightSessionDao : ActiveFlightSessionDao {
        private var session: ActiveFlightSessionEntity? = null

        override suspend fun getActiveFlightSession(sessionId: Int): ActiveFlightSessionEntity? {
            return session
        }

        override suspend fun upsertActiveFlightSession(session: ActiveFlightSessionEntity) {
            this.session = session
        }

        override suspend fun clearActiveFlightSession() {
            session = null
        }
    }

    private val activeFlight = ActiveFlight(
        origin = airport("KOUN", "Norman"),
        destination = airport("KOKC", "Oklahoma City"),
        durationMinutes = 23,
        distanceMiles = 111.0,
        seat = SeatOption(
            id = "10C",
            label = "10C",
            ticketClass = TicketClass.Standard,
            pointsCost = 0,
            description = "Aisle",
        ),
        flightNumber = "TB443",
        startedAt = Instant.parse("2026-05-21T20:00:00Z"),
        plannedArrivalAt = Instant.parse("2026-05-21T20:23:00Z"),
        focusObjective = "Ship session persistence",
        focusTag = "Work",
        status = ActiveFlightStatus.Active,
    )

    private fun airport(code: String, city: String): Airport {
        return Airport(
            code = code,
            icao = code,
            iata = code.takeLast(3),
            name = "$city Airport",
            city = city,
            stateOrRegion = "Oklahoma",
            country = "US",
            latitude = 35.0,
            longitude = -97.0,
            elevationFeet = 1000,
        )
    }
}
