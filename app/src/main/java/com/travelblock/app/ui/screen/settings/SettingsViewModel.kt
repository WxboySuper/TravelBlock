package com.travelblock.app.ui.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelblock.app.data.repository.AirportRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.domain.engine.AirportRouteEngine
import com.travelblock.app.domain.engine.AirportSearchEngine
import com.travelblock.app.domain.model.Airport
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SettingsViewModel(
    private val airportRepository: AirportRepository,
    private val settingsRepository: SettingsRepository,
    private val resetLocalData: suspend () -> Unit,
) : ViewModel() {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    private val allAirports: List<Airport> by lazy { airportRepository.getAllAirports() }

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                uiState = uiState.copy(
                    settings = settings,
                    homeAirport = airportRepository.getAirportByCode(settings.homeAirportCode),
                    currentAirport = airportRepository.getAirportByCode(settings.currentAirportCode),
                    isLoading = false,
                )
                refreshSearchResults()
            }
        }
    }

    fun openAirportPicker(mode: AirportPickerMode) {
        uiState = uiState.copy(
            pickerMode = mode,
            airportSearchQuery = "",
            message = null,
        )
        refreshSearchResults()
    }

    fun dismissAirportPicker() {
        uiState = uiState.copy(
            pickerMode = null,
            airportSearchQuery = "",
        )
    }

    fun onAirportSearchChanged(query: String) {
        uiState = uiState.copy(airportSearchQuery = query)
        refreshSearchResults()
    }

    fun selectAirport(airport: Airport) {
        when (uiState.pickerMode) {
            AirportPickerMode.Home -> {
                uiState = uiState.copy(
                    pickerMode = null,
                    pendingHomeAirport = airport,
                    airportSearchQuery = "",
                )
            }
            AirportPickerMode.Current -> {
                viewModelScope.launch {
                    settingsRepository.setCurrentAirportCode(airport.code)
                    uiState = uiState.copy(
                        pickerMode = null,
                        airportSearchQuery = "",
                        message = "Current airport set to ${airport.code}.",
                    )
                }
            }
            null -> Unit
        }
    }

    fun confirmHomeAirportChange(moveCurrentAirport: Boolean) {
        val airport = uiState.pendingHomeAirport ?: return
        viewModelScope.launch {
            settingsRepository.setHomeAirportCode(airport.code)
            if (moveCurrentAirport) {
                settingsRepository.setCurrentAirportCode(airport.code)
            }
            uiState = uiState.copy(
                pendingHomeAirport = null,
                message = if (moveCurrentAirport) {
                    "Home and current airport set to ${airport.code}."
                } else {
                    "Home airport set to ${airport.code}."
                },
            )
        }
    }

    fun dismissHomeAirportConfirmation() {
        uiState = uiState.copy(pendingHomeAirport = null)
    }

    fun setSelectedTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedTheme(theme)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticsEnabled(enabled)
        }
    }

    fun showResetConfirmation() {
        uiState = uiState.copy(showResetConfirmation = true)
    }

    fun dismissResetConfirmation() {
        uiState = uiState.copy(showResetConfirmation = false)
    }

    fun resetAllLocalData() {
        viewModelScope.launch {
            resetLocalData()
            uiState = uiState.copy(
                showResetConfirmation = false,
                pickerMode = null,
                pendingHomeAirport = null,
                message = "Local data reset. TravelBlock is back at KOUN.",
            )
        }
    }

    private fun refreshSearchResults() {
        val currentAirport = uiState.currentAirport
        val results = AirportSearchEngine.search(
            airports = allAirports,
            query = uiState.airportSearchQuery,
            limit = AirportSearchLimit,
        ).map { airport ->
            airport.toPickerItem(currentAirport)
        }
        uiState = uiState.copy(airportSearchResults = results)
    }

    private fun Airport.toPickerItem(currentAirport: Airport?): AirportPickerItemUiModel {
        val location = listOfNotNull(
            city.takeIf { it.isNotBlank() },
            stateOrRegion?.takeIf { it.isNotBlank() },
            country.takeIf { it.isNotBlank() },
        ).joinToString(", ")
        val distance = currentAirport
            ?.takeIf { it.code != code }
            ?.let { AirportRouteEngine.distanceMiles(it.latitude, it.longitude, latitude, longitude).roundToInt() }

        return AirportPickerItemUiModel(
            airport = this,
            code = code,
            name = name,
            location = location.ifBlank { country },
            distanceLabel = distance?.let { "$it mi from current" },
        )
    }

    companion object {
        private const val AirportSearchLimit = 25
    }
}
