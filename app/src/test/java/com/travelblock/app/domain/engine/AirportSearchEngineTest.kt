package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport
import org.junit.Assert.assertEquals
import org.junit.Test

class AirportSearchEngineTest {
    @Test
    fun searchesByCodeAndIata() {
        val byIcao = AirportSearchEngine.search(airports, "KOUN")
        val byIata = AirportSearchEngine.search(airports, "OUN")

        assertEquals("KOUN", byIcao.first().code)
        assertEquals("KOUN", byIata.first().code)
    }

    @Test
    fun searchesByCityAndName() {
        val byCity = AirportSearchEngine.search(airports, "Tulsa")
        val byName = AirportSearchEngine.search(airports, "Westheimer")

        assertEquals("KTUL", byCity.first().code)
        assertEquals("KOUN", byName.first().code)
    }

    private val airports = listOf(
        airport("KOUN", "OUN", "University of Oklahoma Westheimer Airport", "Norman", "Oklahoma"),
        airport("KTUL", "TUL", "Tulsa International Airport", "Tulsa", "Oklahoma"),
        airport("KDFW", "DFW", "Dallas Fort Worth International Airport", "Dallas-Fort Worth", "Texas"),
    )

    private fun airport(
        code: String,
        iata: String,
        name: String,
        city: String,
        state: String,
    ): Airport {
        return Airport(
            code = code,
            icao = code,
            iata = iata,
            name = name,
            city = city,
            stateOrRegion = state,
            country = "US",
            latitude = 35.0,
            longitude = -97.0,
            elevationFeet = 1_000,
        )
    }
}
