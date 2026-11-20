package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightTelemetryTest {
    private val start = Instant.parse("2026-11-19T18:00:00Z")
    private val seat = SeatOption("10A", "10A", TicketClass.Standard, 0, "Window")

    private fun flight(minutes: Long, miles: Double, dest: Airport = airport("KBBB", 0.0, -90.0)) = ActiveFlight(
        origin = airport("KAAA", 0.0, -97.0), destination = dest, durationMinutes = minutes.toInt(), distanceMiles = miles,
        seat = seat, flightNumber = "TB1", startedAt = start, plannedArrivalAt = start.plusSeconds(minutes * 60),
    )

    private fun airport(code: String, lat: Double, lon: Double) = Airport(code, code, code, code, code, null, "US", lat, lon, null)

    @Test
    fun `a flight starts low and slow, climbing`() {
        val t = FlightTelemetryModel.at(flight(60, 400.0), start)

        assertEquals(FlightStage.Climbing, t.stage)
        assertEquals(0, t.altitudeFeet)
        assertEquals(150, t.groundSpeedMph)
    }

    @Test
    fun `the middle of a long flight is cruise at altitude, in the cold`() {
        val t = FlightTelemetryModel.at(flight(120, 800.0), start.plusSeconds(60 * 60))

        assertEquals(FlightStage.Cruising, t.stage)
        assertEquals(36_000, t.altitudeFeet)
        assertEquals(360, t.groundSpeedMph)
        assertTrue(t.outsideTempF < -60)
    }

    @Test
    fun `short hops cruise lower than long hauls`() {
        val hop = FlightTelemetryModel.at(flight(40, 100.0), start.plusSeconds(20 * 60))
        val regional = FlightTelemetryModel.at(flight(60, 300.0), start.plusSeconds(30 * 60))

        assertEquals(18_000, hop.altitudeFeet)
        assertEquals(28_000, regional.altitudeFeet)
    }

    @Test
    fun `the end of a flight is descending and back near the ground`() {
        val t = FlightTelemetryModel.at(flight(60, 400.0), start.plusSeconds(59 * 60))

        assertEquals(FlightStage.Descending, t.stage)
        assertTrue(t.altitudeFeet < 5_000)
        assertTrue(t.groundSpeedMph < 200)
    }

    @Test
    fun `flying due east points the nose east`() {
        val t = FlightTelemetryModel.at(flight(60, 400.0), start.plusSeconds(30 * 60))

        assertTrue("heading ${t.headingDegrees}", t.headingDegrees in 85..95)
    }

    @Test
    fun `local times follow longitude`() {
        val t = FlightTelemetryModel.at(flight(60, 400.0, airport("KBBB", 0.0, 15.0)), start)

        // 18:00 UTC at -97 degrees is about 11:32 AM; at +15 degrees it is 7:00 PM.
        assertEquals("11:32 AM", t.originLocalTime)
        assertEquals("7:00 PM", t.destinationLocalTime)
    }
}
