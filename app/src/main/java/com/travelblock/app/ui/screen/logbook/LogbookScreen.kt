package com.travelblock.app.ui.screen.logbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.travelblock.app.domain.model.TaskRefs
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.data.repository.FlightLogRepository
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.MetricColumn
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.components.RouteStops
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DisplayFamily
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray

/**
 * @param airportLookup optional code-to-airport lookup (e.g. `container.airportRepository::getAirportByCode`);
 * when null the screen builds one itself from the bundled airport data.
 */
@Composable
fun LogbookScreen(
    flightLogRepository: FlightLogRepository,
    onFlightSelected: (String) -> Unit,
    airportLookup: AirportLookup? = null,
) {
    val viewModel: LogbookViewModel = viewModel(
        factory = LogbookViewModelFactory(flightLogRepository),
    )

    LogbookScreenContent(
        uiState = viewModel.uiState,
        onFlightSelected = onFlightSelected,
        airportLookup = airportLookup,
    )
}

@Composable
fun LogbookScreenContent(
    uiState: LogbookUiState,
    onFlightSelected: (String) -> Unit,
    airportLookup: AirportLookup? = null,
) {
    val lookup = rememberAirportLookup(airportLookup)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item { LogbookHeader() }
        when {
            uiState.isLoading -> item { InfoCard("Opening your logbook...", "Gathering your flight history.") }
            uiState.entries.isEmpty() -> item { EmptyLogbook() }
            else -> filledLogbook(uiState, lookup, onFlightSelected)
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

private fun LazyListScope.filledLogbook(uiState: LogbookUiState, lookup: AirportLookup?, onFlightSelected: (String) -> Unit) {
    uiState.pilot?.let { pilot ->
        item { PilotRankCard(pilot) }
        item { StreakAndWeekRow(pilot) }
    }
        item { SectionTitle(SectionHeading("Where you've been", "Every route you've landed, drawn on the map.")) }
    item { WhereYouveBeenCard(legsFor(uiState.entries, lookup)) }
    item { LifetimeTotalsCard(uiState.summary) }
    item { TravelStatsCard(travelStatsFor(uiState.entries, lookup)) }
    uiState.pilot?.let { pilot ->
        item { PassportCard(pilot.airportsVisited) }
        item { TrophyCaseCard(pilot.unlockedBadges) }
    }
        item { SectionTitle(SectionHeading("Past flights", "Tap a ticket stub to see the details.")) }
    items(uiState.entries, key = { it.id }) { entry ->
        TicketStub(entry = entry, onClick = { onFlightSelected(entry.id) })
    }
}

@Composable
private fun LogbookHeader() {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Kicker("Your travel journal")
        Text("Logbook", style = MaterialTheme.typography.headlineLarge, color = Ink)
    }
}

@Composable
private fun InfoCard(title: String, message: String) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        }
    }
}

@Composable
private fun EmptyLogbook() {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(28.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(64.dp).background(Marigold.copy(alpha = 0.25f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.FlightTakeoff, contentDescription = null, tint = Ink, modifier = Modifier.size(30.dp))
            }
            Text("No flights logged yet", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                "Book a focus flight from Home, land it (or divert), and your journal, passport and trophies start filling in.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ---------- Flight detail ----------

@Composable
fun FlightLogDetailScreen(
    flightId: String,
    flightLogRepository: FlightLogRepository,
    onBack: () -> Unit,
    airportLookup: AirportLookup? = null,
) {
    val viewModel: FlightLogDetailViewModel = viewModel(
        factory = FlightLogDetailViewModelFactory(
            flightId = flightId,
            flightLogRepository = flightLogRepository,
        ),
    )

    FlightLogDetailContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        airportLookup = airportLookup,
    )
}

@Composable
private fun FlightLogDetailContent(
    uiState: FlightLogDetailUiState,
    onBack: () -> Unit,
    airportLookup: AirportLookup?,
) {
    val lookup = rememberAirportLookup(airportLookup)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Cream).statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Kicker("From your journal")
                Text("Flight detail", style = MaterialTheme.typography.headlineLarge, color = Ink)
            }
        }
        val flight = uiState.flight
        when {
            uiState.isLoading -> item { InfoCard("Opening flight...", "Finding this page of your journal.") }
            flight == null -> {
                item { InfoCard("Flight not found", uiState.errorMessage ?: "This logbook entry could not be loaded.") }
                item { SecondaryButton("Back to Logbook", onBack) }
            }
            else -> {
                item { FlightRouteMap(flight, lookup) }
                item { FlightTicket(flight) }
                item { SecondaryButton("Back to Logbook", onBack) }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun FlightRouteMap(flight: FlightLogDetailUiModel, lookup: AirportLookup?) {
    val airportLookup = lookup ?: return
    val from = airportLookup(flight.originCode) ?: return
    val to = airportLookup(flight.destinationCode) ?: return
    if (!from.hasValidCoordinates) return
    if (!to.hasValidCoordinates) return
    Surface(shape = RoundedCornerShape(22.dp), color = Cream, modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(22.dp))) {
        TravelMap(
            stops = RouteStops(
                MapStop(from.displayCode, GeoPoint(from.latitude, from.longitude)),
                MapStop(to.displayCode, GeoPoint(to.latitude, to.longitude)),
            ),
            modifier = Modifier.fillMaxSize(),
            progress = if (flight.isCompleted) 1f else null,
        )
    }
}

@Composable
private fun FlightTicket(flight: FlightLogDetailUiModel) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TicketHeader(flight)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                AirportCode("From", flight.originCode)
                Icon(Icons.Rounded.FlightTakeoff, contentDescription = null, tint = Terracotta, modifier = Modifier.size(28.dp))
                AirportCode("To", flight.destinationCode, Alignment.End)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricColumn("Date", flight.dateLabel)
                MetricColumn("Focus", flight.durationLabel)
                MetricColumn("Distance", flight.distanceLabel)
                MetricColumn("Seat", flight.seatLabel)
                MetricColumn("Points", flight.pointsLabel)
            }
            FocusNote(flight)
            TaskRefs.display(flight.todoistTaskContent)?.let { Text("Task: $it", style = MaterialTheme.typography.bodyMedium, color = WarmGray) }
            Text(flight.detailMessage, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        }
    }
}

@Composable
private fun TicketHeader(flight: FlightLogDetailUiModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Kicker("TravelBlock Air", color = DuskBlue)
            Text(flight.flightNumber, style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        StampPill(flight.statusLabel, color = if (flight.isCompleted) Sage else Terracotta)
    }
}

@Composable
private fun AirportCode(label: String, code: String, align: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = align, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Kicker(label)
        Text(code, fontFamily = DisplayFamily, style = MaterialTheme.typography.displaySmall, color = Ink)
    }
}

@Composable
private fun FocusNote(flight: FlightLogDetailUiModel) {
    if (flight.focusTag.isBlank() && flight.focusObjective.isBlank()) return
    Surface(color = Sand.copy(alpha = 0.45f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (flight.focusTag.isNotBlank()) Kicker(flight.focusTag, color = DuskBlue)
            if (flight.focusObjective.isNotBlank()) {
                Text(flight.focusObjective, style = MaterialTheme.typography.bodyLarge, color = Ink)
            }
        }
    }
}
