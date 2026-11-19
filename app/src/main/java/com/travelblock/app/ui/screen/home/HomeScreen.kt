package com.travelblock.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.SoftChip
import com.travelblock.app.ui.theme.DuskBlue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.LightStatusBarIcons
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.RouteStops
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.UpdateBanner
import com.travelblock.app.update.UpdateUiState
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.scene.FlapText
import com.travelblock.app.ui.scene.TerminalWindows
import com.travelblock.app.ui.scene.localSolarHour
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Lamp
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.WarmGray
import java.time.Instant
import java.time.LocalTime
import kotlinx.coroutines.launch

private val BoardBackground = Color(0xFF1C1B22)
private val BoardLine = Color(0xFF34323C)
private val BoardDim = Color(0xFF8E8A97)

@Composable
fun HomeScreen(
    container: TravelBlockContainer,
    activeFlight: ActiveFlight?,
    onReturnToCockpit: () -> Unit,
    onBookSelectedRoute: (DraftFlightBooking) -> Unit,
    onStartQuickFlight: (DraftFlightBooking) -> Unit = onBookSelectedRoute,
) {
    val updateState by container.updateManager.state.collectAsState()
    val scope = rememberCoroutineScope()
    val updateInfo = (updateState as? UpdateUiState.Available)?.info
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
        updateState = updateState,
        updateInfo = updateInfo,
        onUpdate = container.updateManager::openDownload,
        onDismissUpdate = { updateInfo?.let { info -> scope.launch { container.updateManager.dismiss(info.build) } } },
        activeFlight = activeFlight,
        onDurationSelected = viewModel::onDurationSelected,
        onRouteSelected = viewModel::onRouteSelected,
        onReturnToCockpit = onReturnToCockpit,
        onBookSelectedRoute = { viewModel.createSelectedDraftBooking()?.let(onBookSelectedRoute) },
        onQuickStart = { minutes -> viewModel.createQuickStartDraft(minutes)?.let(onStartQuickFlight) },
        onRebookLastFlight = { viewModel.createRebookDraft()?.let(onStartQuickFlight) },
        onLoadMoreRoutes = viewModel::loadMoreRoutes,
    )
}

/**
 * The terminal concourse: the airport and its sky through big windows, and a flip-style departures
 * board you choose your destination from.
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    updateState: UpdateUiState = UpdateUiState.Idle,
    updateInfo: com.travelblock.app.update.UpdateInfo? = null,
    onUpdate: () -> Unit = {},
    onDismissUpdate: () -> Unit = {},
    activeFlight: ActiveFlight? = null,
    onDurationSelected: (Int) -> Unit,
    onRouteSelected: (String) -> Unit,
    onReturnToCockpit: () -> Unit = {},
    onBookSelectedRoute: () -> Unit,
    onQuickStart: (Int) -> Unit = {},
    onRebookLastFlight: () -> Unit = {},
    onLoadMoreRoutes: () -> Unit = {},
) {
    val hour = remember(uiState.currentAirport) { localSolarHour(Instant.now(), uiState.currentAirport?.longitude ?: -97.0) }
    LightStatusBarIcons()
    Box(Modifier.fillMaxSize().background(Cream)) {
        TerminalWindows(hour, Modifier.fillMaxWidth().height(330.dp))
        Box(Modifier.fillMaxWidth().height(330.dp).background(Brush.verticalGradient(listOf(Color(0x66000000), Color.Transparent))))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()) {
            ConcourseHeader(uiState)
            Spacer(Modifier.height(150.dp))
            UpdateBanner(updateState, updateInfo, onUpdate, onDismissUpdate)
            if (activeFlight != null) {
                InFlightBoard(activeFlight, onReturnToCockpit)
            } else {
                DeparturesBoard(uiState, onDurationSelected, onRouteSelected, onLoadMoreRoutes)
                uiState.selectedRoute?.let { RouteDetails(it, onBookSelectedRoute) }
                FastTrack(uiState.lastFlight != null, onQuickStart, onRebookLastFlight)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConcourseHeader(uiState: HomeUiState) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(greeting().uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.92f))
            Text("Where to?", style = MaterialTheme.typography.displaySmall, color = Color.White)
            uiState.currentAirport?.let {
                Text("Terminal ${it.displayCode} / ${it.city}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.92f))
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StampPill("${uiState.points} pts", color = Lamp, textColor = Color.White)
            uiState.pilotRankLabel?.let { StampPill(it, color = Sage, textColor = Color.White) }
        }
    }
}

@Composable
private fun DeparturesBoard(
    uiState: HomeUiState,
    onDurationSelected: (Int) -> Unit,
    onRouteSelected: (String) -> Unit,
    onLoadMoreRoutes: () -> Unit,
) {
    var showMap by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BoardBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("DEPARTURES", style = MaterialTheme.typography.labelLarge, color = Lamp)
            BoardViewToggle(showMap) { showMap = it }
        }
        DurationTabs(uiState, onDurationSelected)
        Box(Modifier.fillMaxWidth().height(1.dp).background(BoardLine))
        if (showMap && uiState.routes.isNotEmpty()) {
            DeparturesMapPanel(uiState, onRouteSelected)
        } else {
            BoardBody(uiState, onRouteSelected, onLoadMoreRoutes)
        }
    }
}

@Composable
private fun BoardBody(uiState: HomeUiState, onRouteSelected: (String) -> Unit, onLoadMoreRoutes: () -> Unit) {
    if (uiState.isLoading) return BoardMessage("CHECKING THE BOARD...")
    if (uiState.routes.isEmpty()) return BoardMessage(if (uiState.errorMessage != null) "BOARD UNAVAILABLE" else "NO FLIGHTS THAT SHORT. TRY LONGER")
    uiState.visibleRoutes.forEach { route ->
        BoardRow(route, selected = route.id == uiState.selectedRouteId, onClick = { onRouteSelected(route.id) })
    }
    if (uiState.hasMoreRoutes) {
        Text(
            "+ ${uiState.remainingRouteCount} more departures",
            style = MaterialTheme.typography.labelMedium,
            color = Lamp,
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onLoadMoreRoutes).padding(horizontal = 6.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun DurationTabs(uiState: HomeUiState, onDurationSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        uiState.availableDurationsMinutes.forEach { minutes ->
            val selected = minutes == uiState.selectedDurationMinutes
            Text(
                "$minutes MIN",
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) BoardBackground else Lamp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) Lamp else Color.Transparent)
                    .clickable { onDurationSelected(minutes) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun BoardRow(route: HomeRouteCardUiModel, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) Color.White else Lamp
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFF2B2938) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlapText(route.destinationCode, length = 3, width = 24.dp, height = 36.dp, fontSize = 20, color = tint)
        Column(Modifier.weight(1f)) {
            Text(route.destinationCityRegion.substringBefore(",").uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.92f), maxLines = 1)
            Text(route.boardStatus(), style = MaterialTheme.typography.labelSmall, color = if (selected) Lamp else BoardDim)
        }
        FlapText(route.durationMinutes.toString().padStart(2, ' '), length = 2, width = 22.dp, height = 34.dp, fontSize = 18, color = tint)
        Text("MIN", style = MaterialTheme.typography.labelSmall, color = BoardDim)
    }
}

private fun HomeRouteCardUiModel.boardStatus(): String = when {
    durationMinutes <= 10 -> "QUICK HOP / ${distanceLabel.uppercase()}"
    durationMinutes <= 30 -> "ON TIME / ${distanceLabel.uppercase()}"
    else -> "LONG HAUL / ${distanceLabel.uppercase()}"
}

@Composable
private fun BoardMessage(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = Lamp, modifier = Modifier.padding(vertical = 14.dp))
}

@Composable
private fun RouteDetails(route: HomeRouteCardUiModel, onBook: () -> Unit) {
    Column(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Paper),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TravelMap(
                stops = RouteStops(
                    MapStop(route.originCode, GeoPoint(route.originLatitude, route.originLongitude)),
                    MapStop(route.destinationCode, GeoPoint(route.destinationLatitude, route.destinationLongitude)),
                ),
                modifier = Modifier.width(168.dp).height(136.dp),
            )
            Column(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Kicker("Your route")
                Text("${route.originCode} to ${route.destinationCode}", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text(route.destinationName, style = MaterialTheme.typography.bodySmall, color = WarmGray, maxLines = 2)
                Spacer(Modifier.size(4.dp))
                Text("${route.focusTimeLabel} / ${route.distanceLabel}", style = MaterialTheme.typography.labelMedium, color = Ink)
                Text(route.estimatedPointsLabel, style = MaterialTheme.typography.labelMedium, color = Sage)
            }
        }
        PrimaryButton("Check in for ${route.destinationCode}", onBook)
    }
}

@Composable
private fun InFlightBoard(activeFlight: ActiveFlight, onReturnToCockpit: () -> Unit) {
    Column(
        Modifier.padding(horizontal = 14.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(BoardBackground).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("NOW DEPARTED", style = MaterialTheme.typography.labelLarge, color = Lamp)
        FlapText("${activeFlight.origin.displayCode} ${activeFlight.destination.displayCode}", length = 7, width = 30.dp, height = 44.dp, fontSize = 24)
        Text("Flight ${activeFlight.flightNumber} is in the air. Booking reopens when you land.", style = MaterialTheme.typography.bodyMedium, color = Sand)
        PrimaryButton("Back to your seat", onReturnToCockpit)
    }
}

private fun greeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Late night study"
}

/** The priority lane: pick a time and we handle check-in, seat and boarding pass for you. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FastTrack(lastFlightAvailable: Boolean, onQuickStart: (Int) -> Unit, onRebookLastFlight: () -> Unit) {
    var showCustom by remember { mutableStateOf(false) }
    PaperCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Kicker("Fast track", color = Sage)
            Text("Running late? Skip the queue.", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text("We handle check-in, your seat and the boarding pass.", style = MaterialTheme.typography.bodySmall, color = WarmGray)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 25, 45, 60, 90).forEach { SoftChip("$it min", selected = false, onClick = { onQuickStart(it) }) }
                SoftChip("Custom", selected = false, onClick = { showCustom = true })
            }
            if (lastFlightAvailable) {
                Text(
                    "Rebook last flight",
                    style = MaterialTheme.typography.labelLarge,
                    color = DuskBlue,
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onRebookLastFlight).padding(vertical = 6.dp, horizontal = 4.dp),
                )
            }
        }
    }
    if (showCustom) CustomDurationDialog(onDismiss = { showCustom = false }, onConfirm = { showCustom = false; onQuickStart(it) })
}

@Composable
private fun CustomDurationDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var minutes by remember { mutableStateOf("30") }
    val valid = (minutes.toIntOrNull() ?: 0) in 15..180
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom focus flight") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose a flight window from 15 to 180 minutes.")
                OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(3) }, label = { Text("Minutes") }, singleLine = true)
            }
        },
        confirmButton = { Button(enabled = valid, onClick = { onConfirm(minutes.toInt()) }) { Text("Start flight") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
