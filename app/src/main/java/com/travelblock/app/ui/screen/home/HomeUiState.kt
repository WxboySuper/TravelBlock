package com.travelblock.app.ui.screen.home

import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.domain.model.Airport

data class HomeUiState(
    val currentAirport: Airport? = null,
    val points: Int = 0,
    val selectedDurationMinutes: Int = 25,
    val availableDurationsMinutes: List<Int> = listOf(15, 25, 45, 60, 90, 120, 180, 240, 360, 480),
    val lastFlight: FlightLogEntity? = null,
    val routes: List<HomeRouteCardUiModel> = emptyList(),
    val displayedRouteCount: Int = HomeRouteDisplayDefaults.initialCount,
    val selectedRouteId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pilotRankLabel: String? = null,
    val pilotProgressLabel: String? = null,
    /** Airports already landed at, so the board and planner can mark new places. */
    val visitedAirports: Set<String> = emptySet(),
) {
    val selectedRoute: HomeRouteCardUiModel?
        get() = routes.firstOrNull { it.id == selectedRouteId }

    val visibleRoutes: List<HomeRouteCardUiModel>
        get() = routes.take(displayedRouteCount.coerceAtMost(routes.size))

    val hasMoreRoutes: Boolean
        get() = routes.size > displayedRouteCount

    val remainingRouteCount: Int
        get() = (routes.size - displayedRouteCount).coerceAtLeast(0)
}

object HomeRouteDisplayDefaults {
    const val initialCount = 4
    const val loadMoreIncrement = 4
}

data class HomeRouteCardUiModel(
    val id: String,
    val originCode: String,
    val originLatitude: Double,
    val originLongitude: Double,
    val destinationCode: String,
    val destinationIcao: String,
    val destinationName: String,
    val destinationCityRegion: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val distanceMiles: Double,
    val durationMinutes: Int,
    val distanceLabel: String,
    val focusTimeLabel: String,
    val estimatedPointsLabel: String,
    val availabilityLabel: String,
    val legCount: Int = 1,
    val breakMinutes: Int = 0,
    val isNew: Boolean = false,
) {
    val isLongHaul: Boolean get() = legCount > 1
}
