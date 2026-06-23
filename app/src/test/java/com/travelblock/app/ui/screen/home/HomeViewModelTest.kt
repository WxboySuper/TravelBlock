package com.travelblock.app.ui.screen.home

import com.travelblock.app.MainDispatcherRule
import com.travelblock.app.data.repository.AirportRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightRoute
import com.travelblock.app.domain.model.RouteAvailabilityResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun defaultOriginIsKoun() {
        val viewModel = HomeViewModel(FakeAirportRepository())

        assertEquals("KOUN", viewModel.uiState.currentAirport?.code)
        assertEquals(25, viewModel.uiState.selectedDurationMinutes)
        assertTrue(viewModel.uiState.routes.isNotEmpty())
    }

    @Test
    fun changingDurationUpdatesRoutesAndSelection() {
        val viewModel = HomeViewModel(FakeAirportRepository())
        val firstRouteSet = viewModel.uiState.routes.map { it.id }
        val firstSelection = viewModel.uiState.selectedRouteId

        viewModel.onDurationSelected(60)

        assertEquals(60, viewModel.uiState.selectedDurationMinutes)
        assertNotEquals(firstRouteSet, viewModel.uiState.routes.map { it.id })
        assertNotNull(viewModel.uiState.selectedRouteId)
        assertTrue(viewModel.uiState.routes.size > firstRouteSet.size)
    }

    @Test
    fun selectedRouteIsTracked() {
        val viewModel = HomeViewModel(FakeAirportRepository())
        val targetRoute = viewModel.uiState.routes.last()

        viewModel.onRouteSelected(targetRoute.id)

        assertEquals(targetRoute.id, viewModel.uiState.selectedRouteId)
        assertEquals(targetRoute.destinationCode, viewModel.uiState.selectedRoute?.destinationCode)
    }

    @Test
    fun draftBookingUsesRouteEstimatedDurationAndMaxWindow() {
        val viewModel = HomeViewModel(FakeAirportRepository())
        viewModel.onDurationSelected(45)
        val draft = viewModel.createSelectedDraftBooking()

        assertNotNull(draft)
        assertEquals(45, draft?.maxFlightTimeMinutes)
        assertTrue((draft?.durationMinutes ?: 99) <= 45)
        assertTrue((draft?.durationMinutes ?: 0) < 45)
    }

    @Test
    fun loadMoreRoutesRevealsAdditionalDepartures() {
        val viewModel = HomeViewModel(FakeAirportRepository())
        viewModel.onDurationSelected(60)

        assertTrue(viewModel.uiState.routes.size > HomeRouteDisplayDefaults.initialCount)
        assertEquals(HomeRouteDisplayDefaults.initialCount, viewModel.uiState.visibleRoutes.size)
        assertTrue(viewModel.uiState.hasMoreRoutes)

        viewModel.loadMoreRoutes()

        assertTrue(viewModel.uiState.visibleRoutes.size > HomeRouteDisplayDefaults.initialCount)
    }

    @Test
    fun changingDurationResetsDisplayedRouteCount() {
        val viewModel = HomeViewModel(FakeAirportRepository())
        viewModel.onDurationSelected(60)
        viewModel.loadMoreRoutes()
        assertTrue(viewModel.uiState.displayedRouteCount > HomeRouteDisplayDefaults.initialCount)

        viewModel.onDurationSelected(25)

        val expectedDisplayed = minOf(
            HomeRouteDisplayDefaults.initialCount,
            viewModel.uiState.routes.size,
        )
        assertEquals(expectedDisplayed, viewModel.uiState.displayedRouteCount)
    }

    @Test
    fun currentAirportSettingChangesHomeRouteOrigin() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val viewModel = HomeViewModel(
            airportRepository = FakeAirportRepository(),
            settingsRepository = settingsRepository,
        )
        advanceUntilIdle()

        settingsRepository.setCurrentAirportCode("KDFW")
        advanceUntilIdle()

        assertEquals("KDFW", viewModel.uiState.currentAirport?.code)
        assertEquals("KDFW", viewModel.createSelectedDraftBooking()?.origin?.code)
    }

    private class FakeAirportRepository : AirportRepository {
        private val koun = airport("KOUN", "OUN", "University of Oklahoma Westheimer Airport", "Norman")
        private val kokc = airport("KOKC", "OKC", "Will Rogers World Airport", "Oklahoma City")
        private val ktul = airport("KTUL", "TUL", "Tulsa International Airport", "Tulsa")
        private val kdfw = airport("KDFW", "DFW", "Dallas Fort Worth International Airport", "Dallas-Fort Worth")

        override fun getAllAirports(): List<Airport> = listOf(koun, kokc, ktul, kdfw)

        override fun getAirportByCode(code: String): Airport? {
            return getAllAirports().firstOrNull { airport ->
                airport.code == code.uppercase() || airport.iata == code.uppercase()
            }
        }

        override fun getReachableAirports(originCode: String, durationMinutes: Int): List<FlightRoute> {
            val origin = getAirportByCode(originCode) ?: return emptyList()
            val destinations = when {
                durationMinutes >= 60 -> listOf(
                    kdfw, ktul, kokc, kdfw, ktul, kokc, kdfw, ktul, kokc,
                )
                else -> listOf(kokc, ktul)
            }
            return destinations.mapIndexed { index, destination ->
                val estimated = (durationMinutes - (index + 1) * 4).coerceAtLeast(12)
                FlightRoute(
                    origin = origin,
                    destination = destination,
                    distanceMiles = 40.0 + index * 90.0,
                    targetDistanceMiles = durationMinutes * 6.0,
                    selectedDurationMinutes = durationMinutes,
                    estimatedDurationMinutes = estimated,
                    availability = if (index == 0) RouteAvailabilityResult.BestMatch else RouteAvailabilityResult.ShortHop,
                )
            }
        }

        private fun airport(
            code: String,
            iata: String,
            name: String,
            city: String,
        ): Airport {
            return Airport(
                code = code,
                icao = code,
                iata = iata,
                name = name,
                city = city,
                stateOrRegion = "Oklahoma",
                country = "US",
                latitude = 35.0,
                longitude = -97.0,
                elevationFeet = 1_000,
            )
        }
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val state = MutableStateFlow(UserSettings())

        override val settings: Flow<UserSettings> = state

        override suspend fun getSettings(): UserSettings = state.value

        override suspend fun setHomeAirportCode(code: String) {
            state.value = state.value.copy(homeAirportCode = code)
        }

        override suspend fun setCurrentAirportCode(code: String) {
            state.value = state.value.copy(currentAirportCode = code)
        }

        override suspend fun setSelectedTheme(theme: String) {
            state.value = state.value.copy(selectedTheme = theme)
        }

        override suspend fun setSoundEnabled(enabled: Boolean) {
            state.value = state.value.copy(soundEnabled = enabled)
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            state.value = state.value.copy(hapticsEnabled = enabled)
        }

        override suspend fun setEquippedBoardingPassThemeId(id: String) {
            state.value = state.value.copy(equippedBoardingPassThemeId = id)
        }

        override suspend fun setEquippedAircraftIconId(id: String) {
            state.value = state.value.copy(equippedAircraftIconId = id)
        }

        override suspend fun resetToDefaults() {
            state.value = UserSettings()
        }
    }
}
