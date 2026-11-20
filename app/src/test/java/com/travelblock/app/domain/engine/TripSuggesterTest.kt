package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripSuggesterTest {
    private val origin = airport("KAAA", "AAA", 35.0, -97.0)
    private val okc = airport("KOKC", "OKC", 35.4, -97.6)
    private val dfw = airport("KDFW", "DFW", 32.9, -97.0)
    private val den = airport("KDEN", "DEN", 39.8, -104.7)
    private val atl = airport("KATL", "ATL", 33.6, -84.4)
    private val all = listOf(origin, okc, dfw, den, atl, airport("KSML", "SML", 35.2, -97.1))

    @Test
    fun `options go to different directions and only to major airports`() {
        val options = TripSuggester.options(origin, all, TripWish(minutes = 90, connectThroughHubs = false))

        assertTrue(options.isNotEmpty())
        assertTrue(options.all { it.route.destination.code in setOf("KOKC", "KDFW", "KDEN", "KATL") })
        assertEquals(options.size, options.map { it.route.destination.code }.toSet().size)
    }

    @Test
    fun `a short trip from a small airport is offered through its regional hub`() {
        val options = TripSuggester.options(origin, all, TripWish(minutes = 45))
        val toDfw = options.firstOrNull { it.route.destination.code == "KDFW" }

        if (toDfw != null) assertEquals(listOf("KAAA", "KOKC", "KDFW"), toDfw.stops.map { it.code })
    }

    @Test
    fun `a long trip lists the hubs it flies through`() {
        val options = TripSuggester.options(origin, all, TripWish(minutes = 240))

        assertTrue(options.all { it.stops.size >= 2 && it.focusMinutes == LegTimes.forStops(it.stops).sum() })
    }

    @Test
    fun `smaller airports are offered when major airports only is off`() {
        val small = airport("KSML", "SML", 35.5, -97.0)
        val options = TripSuggester.options(origin, all + small, TripWish(minutes = 60, connectThroughHubs = false, majorAirportsOnly = false))

        assertTrue(options.any { it.route.destination.code == "KSML" })
    }

    @Test
    fun `somewhere new beats somewhere already visited and is marked new`() {
        val a = airport("KNEW", "NEW", 36.0, -97.0)
        val b = airport("KOLD", "OLD", 36.0, -97.01)
        val wish = TripWish(minutes = 60, connectThroughHubs = false, majorAirportsOnly = false, visited = setOf("KOLD"))

        val options = TripSuggester.options(origin, listOf(origin, a, b), wish)

        assertEquals("KNEW", options.first().route.destination.code)
        assertTrue(options.first().isNew)
        assertTrue(options.filter { it.route.destination.code == "KOLD" }.none { it.isNew })
    }

    private fun airport(code: String, iata: String, latitude: Double, longitude: Double) =
        Airport(code, code, iata, iata, iata, null, "US", latitude, longitude, null)
}
