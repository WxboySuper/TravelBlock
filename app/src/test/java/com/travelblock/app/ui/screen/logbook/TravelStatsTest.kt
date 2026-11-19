package com.travelblock.app.ui.screen.logbook

import com.travelblock.app.domain.model.Airport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TravelStatsTest {
    private val airports = mapOf(
        "KOUN" to airport("KOUN", "US"),
        "KOKC" to airport("KOKC", "US"),
        "EGLL" to airport("EGLL", "GB"),
    )
    private val lookup: AirportLookup = { airports[it] }

    @Test
    fun `stats count places, the longest flight, the favourite airport and the top tag`() {
        val entries = listOf(
            entry("KOUN", "KOKC", 13.0, "Writing"),
            entry("KOKC", "EGLL", 4500.0, "Writing"),
            entry("EGLL", "KOKC", 4500.0, "Reading"),
            entry("KOKC", "KOUN", 13.0, "").copy(isCompleted = false),
        )

        val stats = travelStatsFor(entries, lookup)

        assertEquals(3, stats.airports)
        assertEquals(2, stats.countries)
        assertEquals(4500, stats.longestMiles)
        assertEquals("KOKC", stats.favouriteAirport)
        assertEquals("Writing", stats.topFocusTag)
    }

    @Test
    fun `an empty logbook has no stats`() {
        val stats = travelStatsFor(emptyList(), lookup)

        assertEquals(0, stats.airports)
        assertNull(stats.favouriteAirport)
        assertNull(stats.topFocusTag)
    }

    private fun entry(from: String, to: String, miles: Double, tag: String) = LogbookEntryUiModel(
        id = "$from$to$miles", flightNumber = "TB1", routeLabel = "$from -> $to", durationLabel = "", distanceLabel = "",
        statusLabel = "", isCompleted = true, pointsLabel = "", dateLabel = "",
        originCode = from, destinationCode = to, distanceMiles = miles, focusTag = tag,
    )

    private fun airport(code: String, country: String) = Airport(code, code, code, code, code, null, country, 0.0, 0.0, null)
}
