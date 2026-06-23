package com.travelblock.app.ui.screen.logbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.travelblock.app.data.repository.FlightLogRepository
import java.time.ZoneId

class LogbookViewModelFactory(
    private val flightLogRepository: FlightLogRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogbookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogbookViewModel(flightLogRepository, zoneId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

