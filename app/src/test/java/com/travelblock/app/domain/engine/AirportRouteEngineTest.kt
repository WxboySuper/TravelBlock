package com.travelblock.app.domain.engine

import com.travelblock.app.data.local.AirportJsonParser
import com.travelblock.app.data.repository.DefaultAirportRepository
import com.travelblock.app.domain.model.Airport
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AirportRouteEngineTest {
    private val airports = AirportJsonParser().parse(readAirportJson())
    private val repository = DefaultAirportRepository(airportProvider = { airports })
    private val engine = AirportRouteEngine()

    @Test
    fun distanceBetweenKounAndKokcIsApproximatelyRegionalHop() {
        val koun = requireNotNull(repository.getAirportByCode("KOUN"))
        val kokc = requireNotNull(repository.getAirportByCode("KOKC"))

        val distanceMiles = engine.distanceMiles(koun, kokc)

        assertTrue("Expected KOUN to KOKC to be about 13 miles, was $distanceMiles", distanceMiles in 10.0..18.0)
    }

    @Test
    fun reachableRoutesExcludeOriginAirport() {
        val routes = repository.getReachableAirports("KOUN", 60)

        assertTrue(routes.isNotEmpty())
        assertTrue(routes.none { it.destination.code == "KOUN" })
    }

    @Test
    fun reachableListChangesWhenDurationIncreases() {
        val fifteenMinuteRoutes = repository.getReachableAirports("KOUN", 15)
        val sixtyMinuteRoutes = repository.getReachableAirports("KOUN", 60)

        assertTrue(fifteenMinuteRoutes.isNotEmpty())
        assertTrue(sixtyMinuteRoutes.isNotEmpty())
        assertTrue(sixtyMinuteRoutes.maxDistance() > fifteenMinuteRoutes.maxDistance())
        assertTrue(sixtyMinuteRoutes.map { it.destination.code }.toSet() != fifteenMinuteRoutes.map { it.destination.code }.toSet())
    }

    @Test
    fun bestMatchesPreferRecognizableLargerAirports() {
        val origin = Airport(
            code = "KAAA",
            icao = "KAAA",
            iata = "AAA",
            name = "Origin Field",
            city = "Origin",
            stateOrRegion = "Test",
            country = "US",
            latitude = 35.0,
            longitude = -97.0,
            elevationFeet = null,
        )
        val internationalAirport = Airport(
            code = "KBBB",
            icao = "KBBB",
            iata = "BBB",
            name = "Blue City International Airport",
            city = "Blue City",
            stateOrRegion = "Test",
            country = "US",
            latitude = 35.02,
            longitude = -96.01,
            elevationFeet = null,
        )
        val smallAirport = Airport(
            code = "KCCC",
            icao = "KCCC",
            iata = "CCC",
            name = "County Airfield",
            city = "County",
            stateOrRegion = "Test",
            country = "US",
            latitude = 35.0,
            longitude = -96.0,
            elevationFeet = null,
        )

        val routes = engine.findReachableRoutes(
            origin = origin,
            airports = listOf(origin, smallAirport, internationalAirport),
            durationMinutes = 30,
        )

        assertEquals("KBBB", routes.first().destination.code)
    }

    @Test
    fun estimatedFlightTimeUsesCruiseMinutesOnly() {
        val route = repository.getReachableAirports("KOUN", 15)
            .first { it.destination.code == "KOKC" }

        assertTrue(
            "KOUN-KOKC should be a short cruise-based flight under 15 min, was ${route.estimatedDurationMinutes}",
            route.estimatedDurationMinutes in 5..14,
        )
    }

    @Test
    fun comparableAirportsWithIataAndIcaoArePrioritized() {
        val origin = Airport(
            code = "KAAA",
            icao = "KAAA",
            iata = "AAA",
            name = "Origin Field",
            city = "Origin",
            stateOrRegion = "Test",
            country = "US",
            latitude = 35.0,
            longitude = -97.0,
            elevationFeet = null,
        )
        val completeCodeAirport = Airport(
            code = "KBBB",
            icao = "KBBB",
            iata = "BBB",
            name = "Blue County Airport",
            city = "Blue",
            stateOrRegion = "Test",
            country = "US",
            latitude = 35.02,
            longitude = -96.01,
            elevationFeet = null,
        )
        val missingIataAirport = Airport(
            code = "KCCC",
            icao = "KCCC",
            iata = null,
            name = "Cedar County Airport",
            city = "Cedar",
            stateOrRegion = "Test",
            country = "US",
            latitude = 35.0,
            longitude = -96.0,
            elevationFeet = null,
        )

        val routes = engine.findReachableRoutes(
            origin = origin,
            airports = listOf(origin, missingIataAirport, completeCodeAirport),
            durationMinutes = 30,
        )

        assertEquals("KBBB", routes.first().destination.code)
    }

    @Test
    fun airportsMissingIataIcaoOrValidCoordinatesAreExcludedFromApp() {
        val validAirport = Airport(
            code = "KBBB",
            icao = "KBBB",
            iata = "BBB",
            name = "Blue City International Airport",
            city = "Blue",
            stateOrRegion = "Test",
            country = "US",
            latitude = 35.1,
            longitude = -96.8,
            elevationFeet = null,
        )
        val missingIata = validAirport.copy(code = "KCCC", icao = "KCCC", iata = null)
        val missingIcao = validAirport.copy(code = "KDDD", icao = "", iata = "DDD")
        val invalidCoordinates = validAirport.copy(code = "KEEE", icao = "KEEE", iata = "EEE", latitude = 120.0)
        val koun = requireNotNull(repository.getAirportByCode("KOUN"))
        val repo = DefaultAirportRepository(airportProvider = {
            listOf(koun, validAirport, missingIata, missingIcao, invalidCoordinates)
        })

        val airports = repo.getAllAirports()
        val routes = repo.getReachableAirports("KOUN", 45)

        assertEquals(listOf("KBBB", "KOUN"), airports.map { it.code }.sorted())
        assertTrue(airports.all { !it.iata.isNullOrBlank() && it.icao.isNotBlank() })
        assertTrue(routes.all { it.destination.isUsableForTravelBlockRouting })
        assertFalse(routes.any { it.destination.code == "KCCC" })
    }

    @Test
    fun maxFlightTimeReturnsOnlyRoutesAtOrBelowSelectedWindow() {
        val routes = repository.getReachableAirports("KOUN", 45)

        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { it.estimatedDurationMinutes <= 45 })
        assertTrue(routes.any { it.estimatedDurationMinutes < 45 })
    }

    @Test
    fun largerMaxFlightTimeIncludesLongerRoutes() {
        val shortRoutes = repository.getReachableAirports("KOUN", 25)
        val longerRoutes = repository.getReachableAirports("KOUN", 60)

        assertTrue(longerRoutes.maxOf { it.estimatedDurationMinutes } > shortRoutes.maxOf { it.estimatedDurationMinutes })
    }

    @Test
    fun invalidAirportCodeReturnsSafeEmptyRoutes() {
        val routes = repository.getReachableAirports("NOPE", 25)

        assertTrue(routes.isEmpty())
    }

    @Test
    fun datasetLoadsExpectedKounFields() {
        val koun: Airport? = repository.getAirportByCode("OUN")

        assertNotNull(koun)
        assertEquals("KOUN", koun?.code)
        assertEquals("University of Oklahoma Westheimer Airport", koun?.name)
    }

    private fun List<com.travelblock.app.domain.model.FlightRoute>.maxDistance(): Double {
        return maxOf { it.distanceMiles }
    }

    private fun readAirportJson(): String {
        val candidates = listOf(
            File("data/airports.json"),
            File("../data/airports.json"),
        )
        return candidates.first { it.exists() }.readText()
    }
}
