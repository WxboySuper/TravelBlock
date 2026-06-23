package com.travelblock.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.travelblock.app.data.repository.AirportRepository
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.settings.SettingsRepository

class HomeViewModelFactory(
    private val airportRepository: AirportRepository,
    private val settingsRepository: SettingsRepository? = null,
    private val pointsRepository: PointsRepository? = null,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(airportRepository, settingsRepository, pointsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
