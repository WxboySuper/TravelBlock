package com.travelblock.app.ui.screen.diversion

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.DiversionSummary
import kotlin.math.roundToInt

private val DiversionBackground = Color(0xFFF7F4EF)
private val DiversionSurface = Color.White
private val DiversionAmber = Color(0xFF9B650F)
private val DiversionAmberSoft = Color(0xFFFFEBC2)
private val DiversionBlue = Color(0xFF1D5F8A)

@Composable
fun DiversionScreen(
    diversionSummary: DiversionSummary?,
    onReturnHome: () -> Unit,
    onTryAgain: () -> Unit,
    onViewLogbook: () -> Unit,
) {
    if (diversionSummary == null) {
        EmptyDiversionState(onReturnHome = onReturnHome)
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DiversionBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "TravelBlock",
                style = MaterialTheme.typography.labelLarge,
                color = DiversionBlue,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Flight Diverted",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "You ended this focus flight early and landed at ${diversionSummary.diversionAirport.code}. It has been saved as a diverted flight.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            DiversionRouteCard(diversionSummary)
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DiversionMetric("Time focused", diversionSummary.focusedDuration.toReadableMinutes())
                DiversionMetric("Miles flown", "${diversionSummary.milesFlown.roundToInt()} mi")
                DiversionMetric("Skipped", "${diversionSummary.milesRemainingSkipped.roundToInt()} mi")
                DiversionMetric("Diversion", "${diversionSummary.distanceToDiversionAirportMiles.roundToInt()} mi")
                DiversionMetric("Points", "+${diversionSummary.pointsEarned}")
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DiversionSurface),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Diversion report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Surface(
                        color = DiversionAmberSoft,
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = "Status: Diverted",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = DiversionAmber,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "${diversionSummary.percentComplete}% of the planned route was flown. Current airport is now ${diversionSummary.diversionAirport.code}, not the intended destination.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onReturnHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Return to Home")
                }
                OutlinedButton(
                    onClick = onTryAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Try Again")
                }
                OutlinedButton(
                    onClick = onViewLogbook,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("View Logbook")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DiversionRouteCard(diversionSummary: DiversionSummary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DiversionSurface),
        shape = RoundedCornerShape(22.dp),
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
                AirportCodeBlock("ORIGIN", diversionSummary.origin.code)
                Text(
                    text = "${diversionSummary.percentComplete}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = DiversionAmber,
                    fontWeight = FontWeight.Bold,
                )
                AirportCodeBlock("DIVERTED", diversionSummary.diversionAirport.code)
            }
            Text(
                text = "Flight ${diversionSummary.flightNumber} planned for ${diversionSummary.intendedDestination.code}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AirportCodeBlock(
    label: String,
    code: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = code,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DiversionMetric(
    label: String,
    value: String,
) {
    Surface(
        color = DiversionSurface,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun EmptyDiversionState(onReturnHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiversionBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DiversionSurface),
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Diversion",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Divert an active focus flight to see the diversion report.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onReturnHome) {
                    Text("Back to Home")
                }
            }
        }
    }
}

private fun java.time.Duration.toReadableMinutes(): String {
    val totalMinutes = toMinutes().coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}
