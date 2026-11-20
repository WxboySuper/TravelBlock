package com.travelblock.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.mutableIntStateOf
import com.travelblock.app.ui.screen.book.formatMinutes
import com.travelblock.app.ui.components.SecondaryButton
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
import androidx.compose.ui.text.style.TextOverflow
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
        planner = TripPlannerHomeActions(
            options = viewModel::tripOptions,
            onPlan = { suggestion -> onBookSelectedRoute(viewModel.draftFor(suggestion)) },
        ),
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
    planner: TripPlannerHomeActions = TripPlannerHomeActions(),
) {
    var quickMinutes by remember { mutableIntStateOf(PlannerDefaultMinutes) }
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
                // The sections below the board sit on a solid sheet so they never show the window picture through them.
                Column(Modifier.fillMaxWidth().background(Cream).padding(top = 28.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(36.dp)) {
                    TripPlannerPanel(uiState.currentAirport, uiState.visitedAirports.size, planner) { quickMinutes = it }
                    FastTrack(FastTrackState(quickMinutes, uiState.lastFlight != null), onQuickStart, onRebookLastFlight)
                }
            }
        }
    }
}

@Composable
private fun ConcourseHeader(uiState: HomeUiState) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(greeting().uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.92f))
            Text("Where to?", style = MaterialTheme.typography.displaySmall, color = Color.White)
            uiState.currentAirport?.let {
                Text("Terminal ${it.displayCode}, ${it.city}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.92f), maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            Text("Departures", style = MaterialTheme.typography.headlineSmall, color = Lamp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DurationTabs(uiState: HomeUiState, onDurationSelected: (Int) -> Unit) {
    // Tabs wrap onto as many rows as the screen and text size need; each label stays on one line.
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        uiState.availableDurationsMinutes.forEach { minutes ->
            DurationTab(minutes, minutes == uiState.selectedDurationMinutes, onDurationSelected)
        }
    }
}

@Composable
private fun DurationTab(minutes: Int, selected: Boolean, onDurationSelected: (Int) -> Unit) {
    Text(
        durationTabLabel(minutes),
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) BoardBackground else Lamp,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Lamp else Color.Transparent)
            .clickable { onDurationSelected(minutes) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun BoardRow(route: HomeRouteCardUiModel, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) Color.White else Lamp
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF2B2938) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlapText(route.destinationCode, length = 3, width = 28.dp, height = 44.dp, fontSize = 24, color = tint)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                route.destinationCityRegion.substringBefore(","),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (route.isNew) NewPlaceTag()
                Text(route.boardMeta(), style = MaterialTheme.typography.bodySmall, color = if (selected) Lamp else BoardDim, maxLines = 1)
            }
        }
        val time = route.durationMinutes.boardTime()
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            FlapText(time.digits, length = time.digits.length, width = 26.dp, height = 40.dp, fontSize = 22, color = tint)
            Text(time.unit.lowercase(), style = MaterialTheme.typography.bodySmall, color = BoardDim)
        }
    }
}

/** A small "New" tag for somewhere you have not landed before. */
@Composable
internal fun NewPlaceTag() {
    Text(
        "New",
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF14131A),
        modifier = Modifier.clip(RoundedCornerShape(50)).background(Lamp).padding(horizontal = 7.dp, vertical = 1.dp),
    )
}

private fun HomeRouteCardUiModel.boardMeta(): String =
    if (isLongHaul) "$legCount flights, $distanceLabel" else distanceLabel

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
                if (route.isNew) Text("A new place for you", style = MaterialTheme.typography.labelMedium, color = Sage)
                Spacer(Modifier.size(4.dp))
                Text("${route.focusTimeLabel} / ${route.distanceLabel}", style = MaterialTheme.typography.labelMedium, color = Ink)
                if (route.isLongHaul) {
                    Text("${route.legCount} flights, ${route.breakMinutes} min of layovers", style = MaterialTheme.typography.labelMedium, color = DuskBlue)
                }
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

/** The priority lane for when you are late: one button that skips check-in for the length chosen in the planner. */
@Composable
private fun FastTrack(fast: FastTrackState, onQuickStart: (Int) -> Unit, onRebookLastFlight: () -> Unit) {
    Column(Modifier.padding(horizontal = 24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Running late?", style = MaterialTheme.typography.headlineSmall, color = Ink)
        Text("Skip check-in. We pick the airport, your seat and the boarding pass.", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        PrimaryButton("Fly ${formatMinutes(fast.minutes)} now", onClick = { onQuickStart(fast.minutes) })
        if (fast.lastFlightAvailable) SecondaryButton("Rebook last flight", onClick = onRebookLastFlight)
    }
}

private data class FastTrackState(val minutes: Int, val lastFlightAvailable: Boolean)

/** The tab label for a focus length: minutes for short ones, hours from two hours up. */
private fun durationTabLabel(minutes: Int): String = if (minutes >= 60) "${minutes / 60} hr" else "$minutes min"

private data class BoardTime(val digits: String, val unit: String)

/** The flip-board time: two digits of minutes, or H:MM for anything long enough not to fit. */
private fun Int.boardTime(): BoardTime =
    if (this < 100) BoardTime(toString().padStart(2, ' '), "MIN") else BoardTime("${this / 60}:${(this % 60).toString().padStart(2, '0')}", "HR")
