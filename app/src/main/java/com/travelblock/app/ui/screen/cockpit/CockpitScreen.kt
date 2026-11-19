package com.travelblock.app.ui.screen.cockpit

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import com.travelblock.app.ui.scene.AirplaneWindow
import com.travelblock.app.ui.scene.cabinDimFor
import com.travelblock.app.ui.scene.localSolarHour
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.components.MapOptions
import com.travelblock.app.ui.components.RouteStops
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray
import kotlinx.coroutines.delay
import java.time.Instant
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
    actions: CockpitActions,
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
            onPrepareForLanding = actions.onPrepareForLanding,
        ),
    )

    FlightUpdateEffect(viewModel.uiState.activeFlight, actions.onFlightUpdated)
    ArrivalEffect(viewModel.uiState.activeFlight, viewModel.uiState.status, container, actions)
    DiversionEffect(viewModel.uiState.status, viewModel.uiState.diversionSummary, actions.onFlightDiverted)

    CockpitScreenContent(
        uiState = viewModel.uiState,
        onDivertRequested = viewModel::requestDivert,
        onDismissDivert = viewModel::dismissDivertDialog,
        onConfirmDivert = viewModel::confirmDivert,
        onSkipLayover = viewModel::skipLayover,
        onBoardNextLeg = viewModel::boardNextLeg,
    )
}

@Composable
private fun FlightUpdateEffect(flight: ActiveFlight?, onFlightUpdated: (ActiveFlight) -> Unit) {
    LaunchedEffect(flight) { flight?.let(onFlightUpdated) }
}

@Composable
private fun ArrivalEffect(
    flight: ActiveFlight?,
    status: ActiveFlightStatus,
    container: TravelBlockContainer,
    actions: CockpitActions,
) {
    var arrivalHandled by remember(flight) { mutableStateOf(false) }
    LaunchedEffect(flight, status) {
        if (flight != null && status == ActiveFlightStatus.Arrived && !arrivalHandled) {
            arrivalHandled = true
            actions.onFlightArrived(flight.toArrivalSummary(container.airportRepository.getAllAirports()))
        }
    }
}

@Composable
private fun DiversionEffect(
    status: ActiveFlightStatus,
    summary: DiversionSummary?,
    onFlightDiverted: (DiversionSummary) -> Unit,
) {
    var diversionHandled by remember(summary) { mutableStateOf(false) }
    LaunchedEffect(status, summary) {
        if (status == ActiveFlightStatus.Diverted && summary != null && !diversionHandled) {
            diversionHandled = true
            onFlightDiverted(summary)
        }
    }
}

data class CockpitActions(
    val onFlightArrived: (ArrivalSummary) -> Unit,
    val onFlightDiverted: (DiversionSummary) -> Unit,
    val onPrepareForLanding: () -> Unit = {},
    val onFlightUpdated: (ActiveFlight) -> Unit = {},
)

private fun ActiveFlight.toArrivalSummary(airports: List<Airport>): ArrivalSummary {
    val legReward = FlightRewardCalculator.completedReward(this)
    val completionBonus = if (isItinerary) com.travelblock.app.domain.engine.ItineraryRewards.COMPLETION_BONUS else 0
    return ArrivalSummary.fromCompletedFlight(this, legReward + completionBonus, airports)
}

/**
 * The view from a window seat. Outside the window the sky follows the real local time over the plane's
 * position; the tray table holds the countdown, a seat-back map and your focus note.
 */
@Composable
fun CockpitScreenContent(
    uiState: CockpitUiState,
    onDivertRequested: () -> Unit,
    onDismissDivert: () -> Unit,
    onConfirmDivert: () -> Unit,
    onSkipLayover: () -> Unit = {},
    onBoardNextLeg: () -> Unit = {},
) {
    TravelBlockTheme(darkTheme = true) {
        val flight = uiState.activeFlight
        if (flight == null) {
            LightStatusIcons(dark = true)
            EmptyFlightDeck()
        } else {
            if (uiState.showDivertDialog) {
                DivertDialog(flight, uiState, onDismissDivert, onConfirmDivert)
            }
            if (uiState.status == ActiveFlightStatus.Layover) {
                LayoverDeck(flight, uiState, onSkipLayover, onBoardNextLeg)
            } else {
                ActiveCockpit(flight, uiState, onDivertRequested)
            }
        }
    }
}

/** Light status-bar icons over a dark cabin, dark icons over a bright one. Restored on leave. */
@Composable
private fun LightStatusIcons(dark: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, dark) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = !dark
        onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
    }
}

@Composable
private fun ActiveCockpit(flight: ActiveFlight, uiState: CockpitUiState, onDivertRequested: () -> Unit) {
    val progress = uiState.progress.coerceIn(0f, 1f)
    val position = FlightRouteInterpolation.interpolate(flight.origin.toGeo(), flight.destination.toGeo(), progress.toDouble())
    val longitude = rememberUpdatedState(position.longitude)
    var hour by remember { mutableFloatStateOf(localSolarHour(Instant.now(), position.longitude)) }
    LaunchedEffect(Unit) {
        while (true) {
            hour = localSolarHour(Instant.now(), longitude.value)
            delay(20_000)
        }
    }
    // A clean switch between a bright day cabin and a dim night cabin keeps text high-contrast at dusk,
    // where a smooth blend would leave mid-grey surfaces behind mid-grey text.
    val night = cabinDimFor(hour) > NIGHT_CABIN_THRESHOLD
    val look = if (night) NightCabin else DayCabin
    LightStatusIcons(dark = night)

    Column(Modifier.fillMaxSize().background(look.wall)) {
        WindowHeader(flight, uiState.status, look, Modifier.statusBarsPadding())
        AirplaneWindow(hour, Modifier.weight(1f).fillMaxWidth().padding(horizontal = 26.dp, vertical = 6.dp))
        TrayTable(flight, uiState, look, onDivertRequested)
    }
}

private class CabinLook(val wall: Color, val tray: Color, val ink: Color, val quiet: Color, val accent: Color)

private const val NIGHT_CABIN_THRESHOLD = 0.4f
private val DayCabin = CabinLook(Color(0xFFD9CFBD), Color(0xFFF1E9DA), Ink, WarmGray, Terracotta)
private val NightCabin = CabinLook(Color(0xFF17151F), Color(0xFF232238), Cream, Sand, Lamp)

@Composable
private fun WindowHeader(flight: ActiveFlight, status: ActiveFlightStatus, look: CabinLook, modifier: Modifier = Modifier) {
    val kicker = when (status) {
        ActiveFlightStatus.Active -> "Seat ${flight.seat.label} / ${flight.flightNumber}"
        ActiveFlightStatus.Layover -> "Layover / Leg ${flight.itineraryLegIndex + 1} of ${flight.itineraryLegCount}"
        ActiveFlightStatus.Arrived -> "Landing"
        ActiveFlightStatus.Diverted -> "Diverted"
    }
    Column(modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
        Kicker(kicker, color = look.quiet)
        Text(
            "Flying to ${flight.destination.city.ifBlank { flight.destination.displayCode }}",
            style = MaterialTheme.typography.headlineSmall,
            color = look.ink,
            maxLines = 1,
        )
    }
}

@Composable
private fun LayoverDeck(
    flight: ActiveFlight,
    uiState: CockpitUiState,
    onSkip: () -> Unit,
    onBoard: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(Cream).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker("${flight.itineraryLabel ?: "Long haul"} / Leg ${flight.itineraryLegIndex + 1} of ${flight.itineraryLegCount}", color = Terracotta)
        Text("Layover in ${flight.destination.city.ifBlank { flight.destination.displayCode }}", style = MaterialTheme.typography.headlineMedium, color = Ink, textAlign = TextAlign.Center)
        Text("Next leg in ${uiState.timeRemainingLabel}", style = MaterialTheme.typography.displaySmall, color = Ink, modifier = Modifier.padding(vertical = 20.dp))
        Text("${flight.destination.displayCode}  to  ${nextLegLabel(flight)}", style = MaterialTheme.typography.titleMedium, color = WarmGray)
        if (uiState.timeRemainingLabel != "00:00") {
            Text("Skip layover", Modifier.clickable(onClick = onSkip).padding(18.dp), color = Terracotta, style = MaterialTheme.typography.labelLarge)
        }
        Text(
            "Board next leg",
            Modifier.clip(RoundedCornerShape(50)).background(if (uiState.timeRemainingLabel == "00:00") Terracotta else Sand)
                .clickable(enabled = uiState.timeRemainingLabel == "00:00", onClick = onBoard).padding(horizontal = 28.dp, vertical = 14.dp),
            color = Ink,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

private fun nextLegLabel(flight: ActiveFlight): String =
    flight.itineraryCodes?.split(',')?.getOrNull(flight.itineraryLegIndex + 2) ?: "next airport"

@Composable
private fun TrayTable(flight: ActiveFlight, uiState: CockpitUiState, look: CabinLook, onDivertRequested: () -> Unit) {
    val shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
    val arrive = uiState.estimatedArrival?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "--"
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(18.dp, shape, clip = false)
            .background(look.tray, shape)
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally).width(46.dp).height(5.dp).background(look.quiet.copy(alpha = 0.4f), RoundedCornerShape(50)))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Kicker("Landing in", color = look.quiet)
                Text(uiState.timeRemainingLabel, style = MaterialTheme.typography.displayLarge, color = look.ink)
            }
            if (flight.focusObjective.isNotBlank() || flight.focusTag.isNotBlank()) StickyNote(flight.focusTag, flight.focusObjective)
        }
        Text("${flight.origin.displayCode}  to  ${flight.destination.displayCode}   /   arrive $arrive", style = MaterialTheme.typography.bodyMedium, color = look.accent)
        PlaneTrack(uiState.progress, look)
        SeatBackScreen(flight, uiState)
        TrayFooter(uiState.status, look, onDivertRequested, Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun TrayFooter(status: ActiveFlightStatus, look: CabinLook, onDivertRequested: () -> Unit, modifier: Modifier = Modifier) {
    if (status == ActiveFlightStatus.Active) {
        Text(
            "End flight early",
            style = MaterialTheme.typography.labelLarge,
            color = look.quiet,
            modifier = modifier.clip(RoundedCornerShape(50)).clickable(onClick = onDivertRequested).padding(horizontal = 18.dp, vertical = 8.dp),
        )
        return
    }
    val message = if (status == ActiveFlightStatus.Arrived) "Touching down..." else "Saved to your logbook"
    Text(message, style = MaterialTheme.typography.labelLarge, color = look.accent, modifier = modifier.padding(vertical = 8.dp))
}

/** A little paper post-it holding what you are working on. */
@Composable
private fun StickyNote(tag: String, objective: String) {
    Column(
        Modifier
            .widthIn(max = 150.dp)
            .graphicsLayer { rotationZ = -3f }
            .shadow(3.dp, RoundedCornerShape(4.dp))
            .background(Color(0xFFFFE58A), RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (tag.isNotBlank()) Text(tag.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color(0xFF7A5B00))
        if (objective.isNotBlank()) Text(objective, style = MaterialTheme.typography.bodySmall, color = Color(0xFF3A2E00), maxLines = 3)
    }
}

/** A dotted line with a plane that slides along it as the flight progresses. */
@Composable
private fun PlaneTrack(progress: Float, look: CabinLook) {
    val fraction = progress.coerceIn(0f, 1f)
    BoxWithConstraints(Modifier.fillMaxWidth().height(26.dp)) {
        val iconSize = 22.dp
        Canvas(Modifier.fillMaxSize()) {
            val y = size.height / 2
            drawLine(look.quiet.copy(alpha = 0.45f), Offset(0f, y), Offset(size.width, y), strokeWidth = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 14f)), cap = StrokeCap.Round)
            drawLine(look.accent, Offset(0f, y), Offset(size.width * fraction, y), strokeWidth = 5f, cap = StrokeCap.Round)
        }
        Icon(
            Icons.Rounded.Flight,
            contentDescription = null,
            tint = look.accent,
            modifier = Modifier.align(Alignment.CenterStart).offset(x = (maxWidth - iconSize) * fraction).size(iconSize).graphicsLayer { rotationZ = 90f },
        )
    }
}

/** The seat-back entertainment screen, showing the live flight map. */
@Composable
private fun SeatBackScreen(flight: ActiveFlight, uiState: CockpitUiState) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF05070E))
            .padding(3.dp)
            .clip(RoundedCornerShape(15.dp)),
    ) {
        TravelMap(
            stops = RouteStops(flight.origin.toStop(), flight.destination.toStop()),
            modifier = Modifier.fillMaxSize(),
            progress = uiState.progress.coerceIn(0f, 1f),
            options = MapOptions(style = TravelMapStyle.Night),
        )
        Text(
            "${uiState.milesRemainingLabel} to go",
            style = MaterialTheme.typography.labelMedium,
            color = Cream,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color(0x99000000), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp),
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

private fun Airport.toGeo() = GeoPoint(latitude, longitude)
