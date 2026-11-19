package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightItinerary
import com.travelblock.app.domain.model.TripPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ItineraryPlannerTest {
    @Test
    fun `planner returns a connecting chain with requested leg count`() {
        val origin = airport("AAA", 0.0, 0.0)
        val destination = airport("ZZZ", 0.0, 30.0)
        val candidates = listOf(airport("HUB", 0.0, 15.0), airport("ALT", 5.0, 20.0))

        val trip = ItineraryPlanner.plan(ItineraryRequest(origin, destination, TripPreset("test", "Test", 2), candidates, 25))

        assertNotNull(trip)
        assertEquals(listOf("AAA", "HUB", "ZZZ"), trip?.airports?.map(Airport::code))
        assertEquals(25, trip?.focusMinutes)
    }

    @Test
    fun `planner rejects impossible route and same airport`() {
        val origin = airport("AAA", 0.0, 0.0)
        val preset = TripPreset("test", "Test", 2)
        assertNull(ItineraryPlanner.plan(ItineraryRequest(origin, origin, preset, emptyList(), 25)))
        assertNull(ItineraryPlanner.plan(ItineraryRequest(origin, airport("ZZZ", 0.0, 30.0), preset, emptyList(), 25)))
    }

    @Test
    fun `transpacific planner can route through a realistic hub`() {
        val origin = airport("KJFK", 40.64, -73.78)
        val destination = airport("RJTT", 35.55, 139.78).copy(iata = "HND", country = "JP")
        val anc = airport("PANC", 61.17, -149.99).copy(iata = "ANC")
        val hnl = airport("PHNL", 21.32, -157.92).copy(iata = "HNL")
        val kef = airport("BIKF", 63.98, -22.61).copy(iata = "KEF", country = "IS")

        val trip = ItineraryPlanner.plan(
            ItineraryRequest(origin, destination, TripPreset("long", "Transpacific", 2), listOf(anc, hnl, kef), 25),
        )

        assertEquals("PANC", trip?.airports?.get(1)?.code)
    }

    @Test
    fun `layover is fifteen minutes after each fourth leg`() {
        assertEquals(FlightItinerary.STANDARD_LAYOVER_MINUTES, FlightItinerary.layoverMinutesAfter(1))
        assertEquals(FlightItinerary.LONG_LAYOVER_MINUTES, FlightItinerary.layoverMinutesAfter(4))
        assertEquals(FlightItinerary.LONG_LAYOVER_MINUTES, FlightItinerary.layoverMinutesAfter(8))
    }

    private fun airport(code: String, latitude: Double, longitude: Double) = Airport(
        code, "K$code", code, code, code, null, "United States", latitude, longitude, null,
    )
}
