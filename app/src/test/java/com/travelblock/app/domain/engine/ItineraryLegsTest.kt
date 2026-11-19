package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import com.travelblock.app.domain.model.ActiveItinerarySchedule
import java.time.Instant
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItineraryLegsTest {
    private val a = airport("KAAA")
    private val b = airport("KBBB")
    private val c = airport("KCCC")
    private val seat = SeatOption("10A", "10A", TicketClass.Standard, 0, "Window")
    private val start = Instant.parse("2026-11-19T12:00:00Z")

    private fun flight(perLeg: Boolean) = ActiveFlight(
        origin = a, destination = b, durationMinutes = 30, distanceMiles = 100.0, seat = seat, flightNumber = "TB1",
        startedAt = start, plannedArrivalAt = start.plusSeconds(1800),
        focusObjective = "Chapter one", focusTag = "Writing", todoistTaskId = "1", todoistTaskContent = "Write",
        itineraryCodes = "KAAA,KBBB,KCCC", itinerarySchedule = ActiveItinerarySchedule(listOf(30, 30), listOf(10)),
        itineraryLegIndex = 0, itineraryLegCount = 2, itineraryTaskPerLeg = perLeg,
    )

    private fun pass() = BoardingPass(
        flightNumber = "TB2", origin = b, destination = c, seat = seat, gate = "A1", boardingGroup = "1",
        departureTime = LocalDateTime.now(), arrivalTime = LocalDateTime.now(), focusDurationMinutes = 30,
        maxFlightTimeMinutes = 30, distanceMiles = 90.0, estimatedRewardPoints = 10,
        focusObjective = "Chapter two", focusTag = "Reading", todoistTaskId = "2", todoistTaskContent = "Read",
    )

    @Test
    fun `when one task covers the trip the next leg keeps the first leg's notes and tasks`() {
        val next = ItineraryLegs.startNextLeg(flight(perLeg = false), pass(), start.plusSeconds(3600))

        assertEquals("Chapter one", next.focusObjective)
        assertEquals("1", next.todoistTaskId)
        assertEquals(1, next.itineraryLegIndex)
        assertEquals("KCCC", next.destination.code)
    }

    @Test
    fun `when each flight has its own tasks the leg takes its own and the trip remembers the rest`() {
        val next = ItineraryLegs.startNextLeg(flight(perLeg = true), pass(), start.plusSeconds(3600))

        assertEquals("Chapter two", next.focusObjective)
        assertEquals("Reading", next.focusTag)
        assertEquals("1,2", next.todoistTaskId)
    }

    @Test
    fun `the next leg is offered as a locked booking that continues the trip`() {
        val draft = ItineraryLegs.nextLegDraft(flight(perLeg = true), listOf(a, b, c))

        assertTrue(draft?.continuation != null)
        assertEquals("KCCC", draft?.destination?.code)
        assertTrue(draft?.tripLocked == true)
    }

    private fun airport(code: String) = Airport(code, code, code.drop(1), code, code, null, "US", 0.0, 0.0, null)
}
