package com.travelblock.app.ui.screen.cockpit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.travelblock.app.data.repository.ActiveFlightSessionRepository
import com.travelblock.app.data.repository.FlightLogRepository
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import java.time.Clock
import java.time.ZoneId

class CockpitViewModelFactory(
    private val activeFlight: ActiveFlight?,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val flightLogRepository: FlightLogRepository? = null,
    private val pointsRepository: PointsRepository? = null,
    private val settingsRepository: SettingsRepository? = null,
    private val activeFlightSessionRepository: ActiveFlightSessionRepository? = null,
    private val airports: List<Airport> = emptyList(),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CockpitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CockpitViewModel(
                activeFlight = activeFlight,
                clock = clock,
                zoneId = zoneId,
                flightLogRepository = flightLogRepository,
                pointsRepository = pointsRepository,
                settingsRepository = settingsRepository,
                activeFlightSessionRepository = activeFlightSessionRepository,
                airports = airports,
                autoTick = true,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
