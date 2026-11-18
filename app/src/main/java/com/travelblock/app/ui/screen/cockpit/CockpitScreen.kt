package com.travelblock.app.ui.screen.cockpit

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.engine.FlightRewardCalculator
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.ArrivalSummary
import com.travelblock.app.domain.model.DiversionSummary
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.components.TravelMapStyle
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.Lamp
import com.travelblock.app.ui.theme.NightLine
import com.travelblock.app.ui.theme.NightSky
import com.travelblock.app.ui.theme.NightSurface
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.TravelBlockTheme
import java.time.format.DateTimeFormatter

@Composable
fun CockpitScreen(
    container: TravelBlockContainer,
    activeFlight: ActiveFlight?,
    onFlightArrived: (ArrivalSummary) -> Unit,
    onFlightDiverted: (DiversionSummary) -> Unit,
) {
    val viewModel: CockpitViewModel = viewModel(
        key = activeFlight?.let { "${it.flightNumber}-${it.startedAt}" } ?: "empty-cockpit",
        factory = CockpitViewModelFactory(
            activeFlight = activeFlight,
            flightLogRepository = container.flightLogRepository,
            pointsRepository = container.pointsRepository,
            settingsRepository = container.settingsRepository,
            activeFlightSessionRepository = container.activeFlightSessionRepository,
            airports = container.airportRepository.getAllAirports(),
        ),
    )

    var arrivalHandled by remember(activeFlight) { mutableStateOf(false) }
    var diversionHandled by remember(activeFlight) { mutableStateOf(false) }

    LaunchedEffect(viewModel.uiState.status, viewModel.uiState.activeFlight) {
        val flight = viewModel.uiState.activeFlight
        if (!arrivalHandled && flight != null && viewModel.uiState.status == ActiveFlightStatus.Arrived) {
            arrivalHandled = true
            onFlightArrived(
                ArrivalSummary.fromCompletedFlight(
                    flight = flight,
                    pointsEarned = FlightRewardCalculator.completedReward(flight),
                ),
            )
        }
    }
    LaunchedEffect(viewModel.uiState.status, viewModel.uiState.diversionSummary) {
        val diversionSummary = viewModel.uiState.diversionSummary
        if (!diversionHandled && diversionSummary != null && viewModel.uiState.status == ActiveFlightStatus.Diverted) {
            diversionHandled = true
            onFlightDiverted(diversionSummary)
        }
    }

    CockpitScreenContent(
        uiState = viewModel.uiState,
        onDivertRequested = viewModel::requestDivert,
        onDismissDivert = viewModel::dismissDivertDialog,
        onConfirmDivert = viewModel::confirmDivert,
    )
}

/**
 * Cozy night-flight focus screen: a live night map fills the screen, the plane moves along the real
 * great-circle route, and a calm card at the bottom holds the countdown.
 */
@Composable
fun CockpitScreenContent(
    uiState: CockpitUiState,
    onDivertRequested: () -> Unit,
    onDismissDivert: () -> Unit,
    onConfirmDivert: () -> Unit,
) {
    TravelBlockTheme(darkTheme = true) {
        LightStatusIconsOff()
        val flight = uiState.activeFlight
        if (flight == null) {
            EmptyFlightDeck()
        } else {
            if (uiState.showDivertDialog) {
                DivertDialog(flight, uiState, onDismissDivert, onConfirmDivert)
            }
            ActiveCockpit(flight, uiState, onDivertRequested)
        }
    }
}

/** The map is dark, so show light status-bar icons while the cockpit is on screen. */
@Composable
private fun LightStatusIconsOff() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
    }
}

@Composable
private fun ActiveCockpit(flight: ActiveFlight, uiState: CockpitUiState, onDivertRequested: () -> Unit) {
    val density = LocalDensity.current
    var cardHeightDp by remember { mutableIntStateOf(300) }
    var headerHeightDp by remember { mutableIntStateOf(100) }

    Box(Modifier.fillMaxSize().background(NightSky)) {
        TravelMap(
            origin = flight.origin.toStop(),
            destination = flight.destination.toStop(),
            modifier = Modifier.fillMaxSize(),
            progress = uiState.progress.coerceIn(0f, 1f),
            style = TravelMapStyle.Night,
            topInset = headerHeightDp,
            bottomInset = cardHeightDp,
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(NightSky, NightSky.copy(alpha = 0.85f), Color.Transparent)))
                .statusBarsPadding()
                .onSizeChanged { with(density) { headerHeightDp = it.height.toDp().value.toInt() } },
        ) {
            CockpitHeader(flight, uiState.status)
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { with(density) { cardHeightDp = it.height.toDp().value.toInt() } },
        ) {
            CockpitCard(flight, uiState, onDivertRequested)
        }
    }
}

@Composable
private fun CockpitHeader(flight: ActiveFlight, status: ActiveFlightStatus) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Kicker(
                when (status) {
                    ActiveFlightStatus.Active -> "Focus flight"
                    ActiveFlightStatus.Arrived -> "Landing"
                    ActiveFlightStatus.Diverted -> "Diverted"
                },
                color = Lamp,
            )
            Text(
                "Flying to ${flight.destination.city.ifBlank { flight.destination.displayCode }}",
                style = MaterialTheme.typography.headlineSmall,
                color = Cream,
                maxLines = 1,
            )
        }
        StampPill(flight.flightNumber, color = Lamp, textColor = Cream)
    }
}

@Composable
private fun CockpitCard(flight: ActiveFlight, uiState: CockpitUiState, onDivertRequested: () -> Unit) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape, clip = false)
            .background(NightSurface, shape)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker("Time until landing", color = Sand)
        Text(
            uiState.timeRemainingLabel,
            style = MaterialTheme.typography.displayLarge,
            color = Cream,
            textAlign = TextAlign.Center,
        )
        Text(
            "${flight.origin.displayCode}  to  ${flight.destination.displayCode}  /  Arrive ${uiState.estimatedArrival?.format(timeFormatter) ?: "--"}",
            style = MaterialTheme.typography.bodyMedium,
            color = Lamp,
        )
        RouteProgressBar(uiState.progress)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CockpitStat("Flown", uiState.milesFlownLabel)
            CockpitStat("To go", uiState.milesRemainingLabel)
            CockpitStat("Seat", flight.seat.label)
        }
        if (flight.focusObjective.isNotBlank() || flight.focusTag.isNotBlank()) {
            FocusNote(flight.focusTag, flight.focusObjective)
        }
        BottomAction(uiState.status, onDivertRequested)
    }
}

@Composable
private fun RouteProgressBar(progress: Float) {
    val fraction = progress.coerceIn(0f, 1f)
    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(NightLine)) {
        Box(Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(50)).background(Lamp))
    }
}

@Composable
private fun CockpitStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Kicker(label, color = Sand)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Cream)
    }
}

@Composable
private fun FocusNote(tag: String, objective: String) {
    Column(
        Modifier.fillMaxWidth().background(NightSky.copy(alpha = 0.55f), RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Kicker("Working on", color = Sand)
        if (tag.isNotBlank()) StampPill(tag, color = Lamp, textColor = Cream)
        if (objective.isNotBlank()) Text(objective, style = MaterialTheme.typography.bodyLarge, color = Cream)
    }
}

/** Ending early is possible, but deliberately quiet so it is never the easiest thing to tap. */
@Composable
private fun BottomAction(status: ActiveFlightStatus, onDivertRequested: () -> Unit) {
    if (status == ActiveFlightStatus.Active) {
        Text(
            "End flight early",
            style = MaterialTheme.typography.labelLarge,
            color = Sand,
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onDivertRequested).padding(horizontal = 18.dp, vertical = 10.dp),
        )
    } else {
        Text(
            if (status == ActiveFlightStatus.Arrived) "Touching down..." else "Saved to your logbook",
            style = MaterialTheme.typography.labelLarge,
            color = Lamp,
        )
    }
}

@Composable
private fun DivertDialog(flight: ActiveFlight, uiState: CockpitUiState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NightSurface,
        titleContentColor = Cream,
        textContentColor = Sand,
        shape = RoundedCornerShape(26.dp),
        title = { Text("Land early?", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${flight.origin.code} to ${flight.destination.code}")
                Text("Focused so far: ${uiState.elapsedLabel}")
                Text("Miles flown: ${uiState.milesFlownLabel}")
                Text("You will land at a nearby airport and keep what you have flown so far. Completion rewards are reduced.")
            }
        },
        confirmButton = {
            Text(
                "Divert flight",
                style = MaterialTheme.typography.labelLarge,
                color = Sand,
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onConfirm).padding(horizontal = 14.dp, vertical = 10.dp),
            )
        },
        dismissButton = {
            Text(
                "Stay focused",
                style = MaterialTheme.typography.labelLarge,
                color = Lamp,
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onDismiss).padding(horizontal = 14.dp, vertical = 10.dp),
            )
        },
    )
}

@Composable
private fun EmptyFlightDeck() {
    Box(Modifier.fillMaxSize().background(NightSky).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier.background(NightSurface, RoundedCornerShape(26.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("No flight in the air", style = MaterialTheme.typography.headlineSmall, color = Cream)
            Text(
                "Check in for a flight from the Home tab and your cockpit will open here.",
                style = MaterialTheme.typography.bodyLarge,
                color = Sand,
            )
        }
    }
}

private fun Airport.toStop() = MapStop(displayCode, GeoPoint(latitude, longitude))
