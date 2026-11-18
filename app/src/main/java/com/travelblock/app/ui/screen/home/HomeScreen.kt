package com.travelblock.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SoftChip
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.WarmGray
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalTime

@Composable
fun HomeScreen(
    container: TravelBlockContainer,
    activeFlight: ActiveFlight?,
    onReturnToCockpit: () -> Unit,
    onBookSelectedRoute: (DraftFlightBooking) -> Unit,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            airportRepository = container.airportRepository,
            settingsRepository = container.settingsRepository,
            pointsRepository = container.pointsRepository,
            flightLogRepository = container.flightLogRepository,
        ),
    )

    HomeScreenContent(
        uiState = viewModel.uiState,
        activeFlight = activeFlight,
        onDurationSelected = viewModel::onDurationSelected,
        onRouteSelected = viewModel::onRouteSelected,
        onReturnToCockpit = onReturnToCockpit,
        onBookSelectedRoute = { viewModel.createSelectedDraftBooking()?.let(onBookSelectedRoute) },
        onLoadMoreRoutes = viewModel::loadMoreRoutes,
    )
}

/**
 * Map-first home: a real map of the chosen route fills the screen, with a warm paper sheet
 * of departures sliding up from the bottom.
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    activeFlight: ActiveFlight? = null,
    onDurationSelected: (Int) -> Unit,
    onRouteSelected: (String) -> Unit,
    onReturnToCockpit: () -> Unit = {},
    onBookSelectedRoute: () -> Unit,
    onLoadMoreRoutes: () -> Unit = {},
) {
    val density = LocalDensity.current
    var sheetHeightDp by remember { mutableIntStateOf(380) }
    var headerHeightDp by remember { mutableIntStateOf(110) }
    val route = uiState.selectedRoute

    Box(Modifier.fillMaxSize().background(Cream)) {
        HomeMap(route, topInset = headerHeightDp, bottomInset = sheetHeightDp)

        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Cream, Cream.copy(alpha = 0.92f), Color.Transparent)))
                .statusBarsPadding()
                .onSizeChanged { with(density) { headerHeightDp = it.height.toDp().value.toInt() } },
        ) {
            HomeHeader(uiState)
        }

        Box(Modifier.align(Alignment.BottomCenter).onSizeChanged { with(density) { sheetHeightDp = it.height.toDp().value.toInt() } }) {
            DepartureSheet(uiState, activeFlight, onDurationSelected, onRouteSelected, onReturnToCockpit, onBookSelectedRoute, onLoadMoreRoutes)
        }
    }
}

@Composable
private fun HomeMap(route: HomeRouteCardUiModel?, topInset: Int, bottomInset: Int) {
    if (route == null) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Cream, Color(0xFFEFE4CD)))))
        return
    }
    TravelMap(
        origin = MapStop(route.originCode, GeoPoint(route.originLatitude, route.originLongitude)),
        destination = MapStop(route.destinationCode, GeoPoint(route.destinationLatitude, route.destinationLongitude)),
        modifier = Modifier.fillMaxSize(),
        topInset = topInset,
        bottomInset = bottomInset,
    )
}

@Composable
private fun HomeHeader(uiState: HomeUiState) {
    val airportLabel = uiState.currentAirport?.let { "${it.city} (${it.displayCode})" }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Kicker(greeting())
            Text("Where to?", style = MaterialTheme.typography.headlineLarge, color = Ink)
            airportLabel?.let { Text("Leaving from $it", style = MaterialTheme.typography.bodySmall, color = WarmGray) }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StampPill("${uiState.points} pts")
            uiState.pilotRankLabel?.let { StampPill(it, color = Sage) }
            uiState.pilotProgressLabel?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = WarmGray) }
        }
    }
}

@Composable
private fun DepartureSheet(
    uiState: HomeUiState,
    activeFlight: ActiveFlight?,
    onDurationSelected: (Int) -> Unit,
    onRouteSelected: (String) -> Unit,
    onReturnToCockpit: () -> Unit,
    onBookSelectedRoute: () -> Unit,
    onLoadMoreRoutes: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), clip = false)
            .background(Paper, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .padding(top = 18.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (activeFlight != null) {
            InFlightNotice(activeFlight, onReturnToCockpit)
            return@Column
        }
        DurationPicker(uiState, onDurationSelected)
        when {
            uiState.isLoading -> SheetMessage("Checking the departures board...", "Finding airports within reach.")
            uiState.errorMessage != null && uiState.routes.isEmpty() -> SheetMessage("The board is blank", uiState.errorMessage)
            uiState.routes.isEmpty() -> SheetMessage("No flights that short", "Try a longer focus window to see more destinations.")
            else -> {
                uiState.selectedRoute?.let { SelectedRouteSummary(it, Modifier.padding(horizontal = 20.dp)) }
                Postcards(uiState, onRouteSelected, onLoadMoreRoutes)
                uiState.selectedRoute?.let {
                    PrimaryButton("Check in for ${it.destinationCode}", onBookSelectedRoute, Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }
}

@Composable
private fun DurationPicker(uiState: HomeUiState, onDurationSelected: (Int) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Kicker("How long can you focus?")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.availableDurationsMinutes.forEach { minutes ->
                SoftChip("$minutes min", selected = minutes == uiState.selectedDurationMinutes, onClick = { onDurationSelected(minutes) })
            }
        }
    }
}

@Composable
private fun SelectedRouteSummary(route: HomeRouteCardUiModel, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text("${route.originCode} to ${route.destinationCode}", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Text(route.destinationCityRegion, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(route.focusTimeLabel, style = MaterialTheme.typography.titleMedium, color = DuskBlue)
            Text("${route.distanceLabel}  /  ${route.estimatedPointsLabel}", style = MaterialTheme.typography.bodySmall, color = WarmGray)
        }
    }
}

@Composable
private fun Postcards(uiState: HomeUiState, onRouteSelected: (String) -> Unit, onLoadMoreRoutes: () -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState, uiState.hasMoreRoutes, uiState.displayedRouteCount) {
        if (!uiState.hasMoreRoutes) return@LaunchedEffect
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 2
        }.distinctUntilChanged().collect { nearEnd -> if (nearEnd) onLoadMoreRoutes() }
    }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(uiState.visibleRoutes, key = { _, r -> r.id }) { _, route ->
            Postcard(route, selected = route.id == uiState.selectedRouteId, onClick = { onRouteSelected(route.id) })
        }
    }
}

@Composable
private fun Postcard(route: HomeRouteCardUiModel, selected: Boolean, onClick: () -> Unit) {
    PaperCard(Modifier.width(128.dp), onClick = onClick, selected = selected, contentPadding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(route.destinationCode, style = MaterialTheme.typography.headlineSmall, color = Ink)
            Text(route.destinationCityRegion.substringBefore(","), style = MaterialTheme.typography.bodySmall, color = WarmGray, maxLines = 1)
            Text(route.focusTimeLabel.replace(" flight", ""), style = MaterialTheme.typography.labelLarge, color = if (selected) DuskBlue else Ink)
        }
    }
}

@Composable
private fun SheetMessage(title: String, message: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
    }
}

@Composable
private fun InFlightNotice(activeFlight: ActiveFlight, onReturnToCockpit: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StampPill("In flight", color = Sage)
        Text(
            "${activeFlight.origin.displayCode} to ${activeFlight.destination.displayCode}",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        Text("Flight ${activeFlight.flightNumber} is in the air. Booking reopens when you land.", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        PrimaryButton("Back to the cockpit", onReturnToCockpit)
    }
}

private fun greeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Late night study"
}
