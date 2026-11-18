package com.travelblock.app.domain.engine

import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.data.database.FlightLogStatus
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PilotProgressionEngineTest {
    private val zone = ZoneId.of("America/Chicago")
    private fun flight(day: String, duration: Int = 60, status: String = FlightLogStatus.COMPLETED.name, distance: Double = 500.0, startHour: Int = 12, from: String = "KOUN", to: String = "KDFW") =
        FlightLogEntity("$day$to", "TB1", from, to, duration, distance, status,
            Instant.parse("${day}T${startHour.toString().padStart(2, '0')}:00:00Z").toEpochMilli(),
            Instant.parse("${day}T23:00:00Z").toEpochMilli(), "12A", 10)

    @Test fun rankProgressUsesCompletedFocusHours() {
        val result = PilotProgressionEngine.summarize(listOf(flight("2026-11-17", duration = 600)), zone, Instant.parse("2026-11-18T18:00:00Z"))
        assertEquals("Private Pilot", result.rank.rank.title)
        assertEquals("Commercial Pilot", result.rank.nextRank?.title)
        assertEquals(40.0, result.rank.hoursRemaining, 0.001)
    }

    @Test fun streakUsesLocalCalendarDaysAndAllowsTodayOrYesterdayStart() {
        val logs = listOf(flight("2026-11-16"), flight("2026-11-17"))
        val summary = PilotProgressionEngine.summarize(logs, zone, Instant.parse("2026-11-18T18:00:00Z"))
        assertEquals(2, summary.currentStreak)
        assertEquals(2, summary.bestStreak)
    }

    @Test fun weeklyStatsAndAirportPassportUseCompletedFlightsOnly() {
        val logs = listOf(flight("2026-11-17", duration = 90, to = "KDFW"), flight("2026-11-16", status = FlightLogStatus.DIVERTED.name, to = "KDAL"))
        val summary = PilotProgressionEngine.summarize(logs, zone, Instant.parse("2026-11-18T18:00:00Z"))
        assertEquals(1, summary.weekly.flights)
        assertEquals(1.5, summary.weekly.hours, 0.001)
        assertEquals(listOf("KDFW", "KOUN"), summary.airportsVisited)
    }

    @Test fun badgesUnlockFromFlightHistory() {
        val logs = (1..10).map { flight("2026-11-${it.toString().padStart(2, '0')}", duration = 60, to = "A$it", startHour = 5, distance = 3500.0) }
        val result = PilotProgressionEngine.summarize(logs, zone, Instant.parse("2026-11-18T18:00:00Z"))
        assertTrue(result.unlockedBadges.containsAll(listOf("First Flight", "Ten Flights", "Red-eye", "Transatlantic", "Seven-day Streak")))
    }
}
