package com.travelblock.app.ui.screen.settings

import com.travelblock.app.MainDispatcherRule
import com.travelblock.app.data.repository.AirportRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun changingHomeAirportPersists() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(
            airportRepository = FakeAirportRepository(),
            settingsRepository = settingsRepository,
            resetLocalData = {},
        )
        advanceUntilIdle()

        viewModel.openAirportPicker(AirportPickerMode.Home)
        viewModel.selectAirport(requireNotNull(FakeAirportRepository().getAirportByCode("KTUL")))
        viewModel.confirmHomeAirportChange(moveCurrentAirport = false)
        advanceUntilIdle()

        assertEquals("KTUL", settingsRepository.getSettings().homeAirportCode)
        assertEquals("KOUN", settingsRepository.getSettings().currentAirportCode)
    }

    @Test
    fun changingCurrentAirportPersistsSeparately() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(
            airportRepository = FakeAirportRepository(),
            settingsRepository = settingsRepository,
            resetLocalData = {},
        )
        advanceUntilIdle()

        viewModel.openAirportPicker(AirportPickerMode.Current)
        viewModel.selectAirport(requireNotNull(FakeAirportRepository().getAirportByCode("KDFW")))
        advanceUntilIdle()

        assertEquals("KOUN", settingsRepository.getSettings().homeAirportCode)
        assertEquals("KDFW", settingsRepository.getSettings().currentAirportCode)
    }

    @Test
    fun resetLocalDataResetsSettings() = runTest {
        val settingsRepository = FakeSettingsRepository(UserSettings(homeAirportCode = "KTUL", currentAirportCode = "KDFW"))
        var resetCalled = false
        val viewModel = SettingsViewModel(
            airportRepository = FakeAirportRepository(),
            settingsRepository = settingsRepository,
            resetLocalData = {
                resetCalled = true
                settingsRepository.resetToDefaults()
            },
        )
        advanceUntilIdle()

        viewModel.resetAllLocalData()
        advanceUntilIdle()

        assertTrue(resetCalled)
        assertEquals("KOUN", settingsRepository.getSettings().homeAirportCode)
        assertEquals("KOUN", settingsRepository.getSettings().currentAirportCode)
    }

    private class FakeAirportRepository : AirportRepository {
        private val airports = listOf(
            airport("KOUN", "OUN", "University of Oklahoma Westheimer Airport", "Norman", "Oklahoma"),
            airport("KTUL", "TUL", "Tulsa International Airport", "Tulsa", "Oklahoma"),
            airport("KDFW", "DFW", "Dallas Fort Worth International Airport", "Dallas-Fort Worth", "Texas"),
        )

        override fun getAllAirports(): List<Airport> = airports

        override fun getAirportByCode(code: String): Airport? {
            val normalized = code.uppercase()
            return airports.firstOrNull { it.code == normalized || it.icao == normalized || it.iata == normalized }
        }

        override fun getReachableAirports(originCode: String, durationMinutes: Int): List<FlightRoute> = emptyList()

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

    private class FakeSettingsRepository(initialSettings: UserSettings = UserSettings()) : SettingsRepository {
        private val settingsFlow = MutableStateFlow(initialSettings)

        override val settings: Flow<UserSettings> = settingsFlow

        override suspend fun getSettings(): UserSettings = settingsFlow.value

        override suspend fun setHomeAirportCode(code: String) {
            settingsFlow.value = settingsFlow.value.copy(homeAirportCode = code)
        }

        override suspend fun setCurrentAirportCode(code: String) {
            settingsFlow.value = settingsFlow.value.copy(currentAirportCode = code)
        }

        override suspend fun setSelectedTheme(theme: String) {
            settingsFlow.value = settingsFlow.value.copy(selectedTheme = theme)
        }

        override suspend fun setSoundEnabled(enabled: Boolean) {
            settingsFlow.value = settingsFlow.value.copy(soundEnabled = enabled)
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            settingsFlow.value = settingsFlow.value.copy(hapticsEnabled = enabled)
        }

        override suspend fun setEquippedBoardingPassThemeId(id: String) {
            settingsFlow.value = settingsFlow.value.copy(equippedBoardingPassThemeId = id)
        }

        override suspend fun setEquippedAircraftIconId(id: String) {
            settingsFlow.value = settingsFlow.value.copy(equippedAircraftIconId = id)
        }

        override suspend fun resetToDefaults() {
            settingsFlow.value = UserSettings()
        }
    }
}
