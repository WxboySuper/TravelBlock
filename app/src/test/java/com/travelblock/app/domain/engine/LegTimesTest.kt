package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegTimesTest {
    private fun airport(code: String, lat: Double, lon: Double) = Airport(code, code, code, code, code, null, "US", lat, lon, null)

    @Test
    fun `a flight takes as long as its distance says`() {
        val engine = AirportRouteEngine()
        val from = airport("AAA", 36.0, -96.0)
        val to = airport("BBB", 35.4, -97.6)

        assertEquals(listOf(engine.estimatedDurationMinutes(engine.distanceMiles(from, to))), LegTimes.forStops(listOf(from, to)))
    }

    @Test
    fun `a ten mile hop is short and a long leg is long`() {
        val origin = airport("RVS", 36.04, -95.98)
        val sameCity = airport("TUL", 36.20, -95.89)
        val faraway = airport("DEN", 39.86, -104.67)

        val (hop, haul) = LegTimes.forStops(listOf(origin, sameCity, faraway))

        assertTrue("hop was $hop", hop <= 10)
        assertTrue("haul was $haul", haul in 60..120)
    }
}
