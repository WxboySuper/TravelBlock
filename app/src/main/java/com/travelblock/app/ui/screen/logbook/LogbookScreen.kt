package com.travelblock.app.ui.screen.logbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.data.repository.FlightLogRepository
import com.travelblock.app.ui.components.EmptyState
import com.travelblock.app.ui.components.SectionHeader
import com.travelblock.app.ui.components.StatusChip
import com.travelblock.app.ui.components.StatusTone
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.MutedText
import com.travelblock.app.ui.theme.SoftSky

@Composable
fun LogbookScreen(
    flightLogRepository: FlightLogRepository,
    onFlightSelected: (String) -> Unit,
) {
    val viewModel: LogbookViewModel = viewModel(
        factory = LogbookViewModelFactory(flightLogRepository),
    )

    LogbookScreenContent(
        uiState = viewModel.uiState,
        onFlightSelected = onFlightSelected,
    )
}

@Composable
fun LogbookScreenContent(
    uiState: LogbookUiState,
    onFlightSelected: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "Logbook",
                subtitle = "Your completed and diverted focus flights.",
            )
        }
        if (uiState.isLoading) {
            item { EmptyState(title = "Logbook", message = "Loading flight history...") }
        } else if (uiState.entries.isEmpty()) {
            item {
                EmptyState(
                    title = "No flights logged yet",
                    message = "Book a focus flight from Home, complete or divert it, and your travel history will appear here.",
                )
            }
        } else {
            item { LogbookSummaryCard(uiState.summary) }
            items(uiState.entries, key = { it.id }) { entry ->
                LogbookEntryCard(
                    entry = entry,
                    onClick = { onFlightSelected(entry.id) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun LogbookSummaryCard(summary: LogbookSummaryUiModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Flight totals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryMetric("Flights", summary.totalFlightsLabel)
                SummaryMetric("Completed", summary.completedFlightsLabel)
                SummaryMetric("Diverted", summary.divertedFlightsLabel)
                SummaryMetric("Focus", summary.totalFocusTimeLabel)
                SummaryMetric("Miles", summary.totalMilesLabel)
                SummaryMetric("Points", summary.totalPointsLabel)
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
) {
    Surface(
        color = SoftSky,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = label, color = MutedText, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LogbookEntryCard(
    entry: LogbookEntryUiModel,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.flightNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                StatusChip(
                    text = entry.statusLabel,
                    tone = if (entry.isCompleted) StatusTone.Success else StatusTone.Warning,
                )
            }
            Text(
                text = entry.routeLabel,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(entry.durationLabel, color = MutedText, style = MaterialTheme.typography.bodyMedium)
                Text(entry.distanceLabel, color = MutedText, style = MaterialTheme.typography.bodyMedium)
                Text(entry.dateLabel, color = MutedText, style = MaterialTheme.typography.bodyMedium)
                Text(entry.pointsLabel, color = CabinAmber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyLogbookCard(
    title: String = "Logbook",
    message: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MutedText,
            )
        }
    }
}

@Composable
fun FlightLogDetailScreen(
    flightId: String,
    flightLogRepository: FlightLogRepository,
    onBack: () -> Unit,
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
    )
}

@Composable
private fun FlightLogDetailContent(
    uiState: FlightLogDetailUiState,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Flight Detail",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        if (uiState.isLoading) {
            item { EmptyLogbookCard(message = "Loading flight detail...") }
        } else if (uiState.flight == null) {
            item {
                EmptyLogbookCard(
                    title = "Flight not found",
                    message = uiState.errorMessage ?: "This logbook entry could not be loaded.",
                )
            }
            item {
                androidx.compose.material3.OutlinedButton(onClick = onBack) {
                    Text("Back to Logbook")
                }
            }
        } else {
            item { FlightDocumentCard(uiState.flight) }
            item {
                androidx.compose.material3.OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Back to Logbook")
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun FlightDocumentCard(flight: FlightLogDetailUiModel) {
    val statusColor = if (flight.isCompleted) MaterialTheme.colorScheme.primary else CabinAmber
    val statusSurface = if (flight.isCompleted) SoftSky else CabinAmber.copy(alpha = 0.16f)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("TravelBlock", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(flight.flightNumber, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Surface(color = statusSurface, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = flight.statusLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AirportBlock("FROM", flight.originCode)
                AirportBlock("TO", flight.destinationCode)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryMetric("Date", flight.dateLabel)
                SummaryMetric("Focus", flight.durationLabel)
                SummaryMetric("Distance", flight.distanceLabel)
                SummaryMetric("Seat", flight.seatLabel)
                SummaryMetric("Points", flight.pointsLabel)
            }
            if (flight.focusTag.isNotBlank() || flight.focusObjective.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (flight.focusTag.isNotBlank()) {
                        StatusChip(flight.focusTag, tone = StatusTone.Info)
                    }
                }
                if (flight.focusObjective.isNotBlank()) {
                    Text(
                        text = flight.focusObjective,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                text = flight.detailMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText,
            )
        }
    }
}

@Composable
private fun AirportBlock(
    label: String,
    code: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MutedText, fontWeight = FontWeight.SemiBold)
        Text(code, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}
