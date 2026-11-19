package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HubRoutingTest {
    private val small1 = airport("KAAA", "AAA", 35.0, -97.0)
    private val small2 = airport("KBBB", "BBB", 36.0, -95.0)
    private val okc = airport("KOKC", "OKC", 35.4, -97.6)

    @Test
    fun `two small airports can connect through a nearby major hub and it costs extra time`() {
        val connection = HubRouting.connect(small1, small2, listOf(small1, small2, okc))

        assertNotNull(connection)
        assertEquals("KOKC", connection?.hub?.code)
        assertTrue(connection!!.extraMinutes > 0)
        assertEquals(connection.firstLegMinutes + connection.secondLegMinutes, connection.totalFocusMinutes)
    }

    @Test
    fun `no connection is offered when an end of the trip is already a major airport`() {
        assertNull(HubRouting.connect(okc, small2, listOf(small1, small2, okc)))
        assertNull(HubRouting.connect(small1, okc, listOf(small1, small2, okc)))
    }

    @Test
    fun `no connection is offered when the only hub is a huge detour`() {
        val farHub = airport("KJFK", "JFK", 40.6, -73.8)

        assertNull(HubRouting.connect(small1, small2, listOf(small1, small2, farHub)))
    }

    @Test
    fun `hub itinerary has two legs with the layover between them`() {
        val connection = requireNotNull(HubRouting.connect(small1, small2, listOf(small1, small2, okc)))

        val itinerary = HubRouting.itinerary(small1, small2, connection)

        assertEquals(listOf("KAAA", "KOKC", "KBBB"), itinerary.airports.map { it.code })
        assertEquals(listOf(connection.layoverMinutes), itinerary.breakDurationsMinutes)
    }

    private fun airport(code: String, iata: String, latitude: Double, longitude: Double) =
        Airport(code, code, iata, iata, iata, null, "US", latitude, longitude, null)
}
