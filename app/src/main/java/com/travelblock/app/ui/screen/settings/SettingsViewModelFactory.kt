package com.travelblock.app.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.travelblock.app.data.repository.AirportRepository
import com.travelblock.app.data.settings.SettingsRepository

class SettingsViewModelFactory(
    private val airportRepository: AirportRepository,
    private val settingsRepository: SettingsRepository,
    private val resetLocalData: suspend () -> Unit,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                airportRepository = airportRepository,
                settingsRepository = settingsRepository,
                resetLocalData = resetLocalData,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
