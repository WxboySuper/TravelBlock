package com.travelblock.app.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AirportTest {
    @Test
    fun requiresBothIataAndIcao() {
        val complete = Airport(
            code = "KOUN",
            icao = "KOUN",
            iata = "OUN",
            name = "Test",
            city = "Norman",
            stateOrRegion = "OK",
            country = "US",
            latitude = 35.0,
            longitude = -97.0,
            elevationFeet = null,
        )
        val missingIata = complete.copy(iata = null)
        val missingIcao = complete.copy(icao = "")

        assertTrue(complete.isUsableForTravelBlockRouting)
        assertFalse(missingIata.isUsableForTravelBlockRouting)
        assertFalse(missingIcao.isUsableForTravelBlockRouting)
    }
}
