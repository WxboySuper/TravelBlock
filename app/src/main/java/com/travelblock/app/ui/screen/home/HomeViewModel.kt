package com.travelblock.app.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelblock.app.data.repository.AirportRepository
import com.travelblock.app.data.repository.FlightLogRepository
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.domain.engine.FlightRewardCalculator
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.FlightRoute
import com.travelblock.app.domain.model.RouteAvailabilityResult
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class HomeViewModel(
    private val airportRepository: AirportRepository,
    private val settingsRepository: SettingsRepository? = null,
    private val pointsRepository: PointsRepository? = null,
    private val flightLogRepository: FlightLogRepository? = null,
) : ViewModel() {
    var uiState by mutableStateOf(HomeUiState(isLoading = true))
        private set
    private var reachableRoutes: List<FlightRoute> = emptyList()

    init {
        loadHome()
        observePoints()
        observeLastFlight()
    }

    fun onDurationSelected(durationMinutes: Int) {
        if (durationMinutes == uiState.selectedDurationMinutes) return

        uiState = uiState.copy(
            selectedDurationMinutes = durationMinutes,
            selectedRouteId = null,
            displayedRouteCount = HomeRouteDisplayDefaults.initialCount,
        )
        refreshRoutes()
    }

    fun loadMoreRoutes() {
        if (!uiState.hasMoreRoutes) return
        val nextCount = (uiState.displayedRouteCount + HomeRouteDisplayDefaults.loadMoreIncrement)
            .coerceAtMost(uiState.routes.size)
        uiState = uiState.copy(displayedRouteCount = nextCount)
    }

    fun onRouteSelected(routeId: String) {
        if (uiState.routes.none { it.id == routeId }) return
        uiState = uiState.copy(selectedRouteId = routeId)
    }

    fun createSelectedDraftBooking(): DraftFlightBooking? {
        val selectedRouteId = uiState.selectedRouteId ?: return null
        val route = reachableRoutes.firstOrNull { it.destination.code == selectedRouteId } ?: return null
        return route.toDraftBooking(uiState.selectedDurationMinutes)
    }

    fun createQuickStartDraft(durationMinutes: Int = uiState.selectedDurationMinutes): DraftFlightBooking? {
        val origin = uiState.currentAirport ?: return null
        val route = airportRepository.getReachableAirports(origin.code, durationMinutes)
            .minByOrNull { abs(durationMinutes - it.estimatedDurationMinutes) }
            ?: return null
        return route.toDraftBooking(durationMinutes)
    }

    fun createRebookDraft(): DraftFlightBooking? {
        val previous = uiState.lastFlight ?: return null
        val duration = previous.durationMinutes.coerceIn(15, 180)
        val routes = airportRepository.getReachableAirports(
            originCode = uiState.currentAirport?.code ?: return null,
            durationMinutes = maxOf(duration, uiState.selectedDurationMinutes),
        )
        val route = routes.firstOrNull { it.destination.code == previous.destinationCode }
            ?: routes.firstOrNull()
            ?: return null
        return route.toDraftBooking(maxOf(duration, uiState.selectedDurationMinutes))
    }

    private fun FlightRoute.toDraftBooking(maxDurationMinutes: Int): DraftFlightBooking {
        val rewardPoints = FlightRewardCalculator.completedReward(
            durationMinutes = estimatedDurationMinutes,
            distanceMiles = distanceMiles,
        )
        return DraftFlightBooking(
            origin = origin,
            destination = destination,
            durationMinutes = estimatedDurationMinutes,
            distanceMiles = distanceMiles,
            estimatedRewardPoints = rewardPoints,
            maxFlightTimeMinutes = maxDurationMinutes,
        )
    }

    private fun loadHome() {
        if (settingsRepository == null) {
            val currentAirport = airportRepository.getAirportByCode(DefaultOriginCode)
            uiState = uiState.copy(
                currentAirport = currentAirport,
                isLoading = false,
                errorMessage = if (currentAirport == null) "KOUN could not be loaded." else null,
            )
            refreshRoutes()
            return
        }

        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                val currentCode = settings.currentAirportCode
                if (!uiState.isLoading && uiState.currentAirport?.code == currentCode) return@collect

                val currentAirport = airportRepository.getAirportByCode(currentCode)
                uiState = uiState.copy(
                    currentAirport = currentAirport,
                    isLoading = false,
                    selectedRouteId = null,
                    displayedRouteCount = HomeRouteDisplayDefaults.initialCount,
                    errorMessage = if (currentAirport == null) "$currentCode could not be loaded." else null,
                )
                refreshRoutes()
            }
        }
    }

    private fun observePoints() {
        val pointsRepository = pointsRepository ?: return
        viewModelScope.launch {
            pointsRepository.observePointsBalance().collect { points ->
                uiState = uiState.copy(points = points)
            }
        }
    }

    private fun observeLastFlight() {
        val repository = flightLogRepository ?: return
        viewModelScope.launch {
            repository.observeFlightLogs().collect { flights ->
                uiState = uiState.copy(lastFlight = flights.firstOrNull())
            }
        }
    }

    private fun refreshRoutes() {
        val originCode = uiState.currentAirport?.code ?: DefaultOriginCode
        reachableRoutes = airportRepository.getReachableAirports(
            originCode = originCode,
            durationMinutes = uiState.selectedDurationMinutes,
        )
        val routes = reachableRoutes.map { route -> route.toUiModel() }
        val displayedCount = when {
            routes.isEmpty() -> HomeRouteDisplayDefaults.initialCount
            else -> uiState.displayedRouteCount.coerceAtMost(routes.size)
        }

        uiState = uiState.copy(
            routes = routes,
            displayedRouteCount = displayedCount,
            selectedRouteId = routes.firstOrNull()?.id,
            errorMessage = when {
                uiState.currentAirport == null -> uiState.errorMessage
                routes.isEmpty() -> "No airports fit within ${uiState.selectedDurationMinutes} min. Try a longer max flight time."
                else -> null
            },
        )
    }

    private fun FlightRoute.toUiModel(): HomeRouteCardUiModel {
        val region = destination.stateOrRegion?.takeIf { it.isNotBlank() }
        val cityRegion = listOfNotNull(destination.city.takeIf { it.isNotBlank() }, region)
            .joinToString(", ")
            .ifBlank { destination.country }
        val roundedDistance = distanceMiles.roundToInt()
        val points = FlightRewardCalculator.completedReward(
            durationMinutes = estimatedDurationMinutes,
            distanceMiles = distanceMiles,
        )

        return HomeRouteCardUiModel(
            id = destination.code,
            originCode = origin.displayCode,
            originLatitude = origin.latitude,
            originLongitude = origin.longitude,
            destinationCode = destination.displayCode,
            destinationIcao = destination.icao,
            destinationName = destination.name,
            destinationCityRegion = cityRegion,
            destinationLatitude = destination.latitude,
            destinationLongitude = destination.longitude,
            distanceMiles = distanceMiles,
            durationMinutes = estimatedDurationMinutes,
            distanceLabel = "$roundedDistance mi",
            focusTimeLabel = "${estimatedDurationMinutes} min",
            estimatedPointsLabel = "+$points pts",
            availabilityLabel = availability.toDisplayLabel(),
        )
    }

    private fun RouteAvailabilityResult.toDisplayLabel(): String {
        return when (this) {
            RouteAvailabilityResult.BestMatch -> "Best match"
            RouteAvailabilityResult.ShortHop -> "Short hop"
            RouteAvailabilityResult.LongStretch -> "Long stretch"
        }
    }

    companion object {
        const val DefaultOriginCode = "KOUN"
    }
}
