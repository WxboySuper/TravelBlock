package com.travelblock.app.ui.screen.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.repository.StoreRepository
import com.travelblock.app.data.settings.SettingsRepository
import java.time.Clock

class StoreViewModelFactory(
    private val pointsRepository: PointsRepository,
    private val storeRepository: StoreRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreViewModel(pointsRepository, storeRepository, settingsRepository, clock) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
