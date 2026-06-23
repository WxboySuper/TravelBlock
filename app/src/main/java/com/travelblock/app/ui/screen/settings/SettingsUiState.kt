package com.travelblock.app.ui.screen.settings

import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.model.Airport

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val homeAirport: Airport? = null,
    val currentAirport: Airport? = null,
    val airportSearchQuery: String = "",
    val airportSearchResults: List<AirportPickerItemUiModel> = emptyList(),
    val pickerMode: AirportPickerMode? = null,
    val pendingHomeAirport: Airport? = null,
    val showResetConfirmation: Boolean = false,
    val message: String? = null,
    val isLoading: Boolean = true,
)

data class AirportPickerItemUiModel(
    val airport: Airport,
    val code: String,
    val name: String,
    val location: String,
    val distanceLabel: String?,
)

enum class AirportPickerMode {
    Home,
    Current,
}
