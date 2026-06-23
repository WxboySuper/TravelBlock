package com.travelblock.app.ui.screen.arrival

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
import com.travelblock.app.ui.components.StickyActionScaffold
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.ArrivalSummary
import com.travelblock.app.ui.components.FlightProgressMapView
import kotlin.math.roundToInt
import com.travelblock.app.ui.components.buildFlightMapModel

private val ArrivalBackground = Color(0xFFF5F8FB)
private val ArrivalBlue = Color(0xFF1D5F8A)
private val ArrivalBlueSoft = Color(0xFFE5F2F8)
private val ArrivalGreen = Color(0xFF287A55)
private val ArrivalAmberSoft = Color(0xFFFFF3D7)

@Composable
fun ArrivalScreen(
    arrivalSummary: ArrivalSummary?,
    homeAirportCode: String,
    onContinueJourney: () -> Unit,
    onViewLogbook: () -> Unit,
) {
    var showReturnHomePlaceholder by remember { mutableStateOf(false) }

    if (arrivalSummary == null) {
        EmptyArrivalState(onContinueJourney = onContinueJourney)
        return
    }

    StickyActionScaffold(
        modifier = Modifier.background(ArrivalBackground),
        stickyAction = {
            Button(
                onClick = onContinueJourney,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Continue Journey")
            }
            OutlinedButton(
                onClick = { showReturnHomePlaceholder = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Return Home")
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
        },
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "TravelBlock",
                style = MaterialTheme.typography.labelLarge,
                color = ArrivalBlue,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Arrived at ${arrivalSummary.destination.code}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = arrivalSummary.destination.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val cityLine = listOfNotNull(
                arrivalSummary.destination.city.takeIf { it.isNotBlank() },
                arrivalSummary.destination.stateOrRegion?.takeIf { it.isNotBlank() },
            ).joinToString(", ")
            if (cityLine.isNotBlank()) {
                Text(
                    text = cityLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            ArrivalHeroCard(arrivalSummary)
        }
        item {
            FlightProgressMapView(
                flightMap = buildFlightMapModel(
                    origin = arrivalSummary.origin,
                    destination = arrivalSummary.destination,
                    progress = 1.0,
                ),
                originCode = arrivalSummary.origin.displayCode,
                destinationCode = arrivalSummary.destination.displayCode,
                progressLabel = "${arrivalSummary.durationMinutes} min · ${arrivalSummary.distanceMiles.roundToInt()} mi",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
        }
        if (arrivalSummary.focusObjective.isNotBlank() || arrivalSummary.focusTag.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Focus cargo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (arrivalSummary.focusTag.isNotBlank()) {
                            Surface(
                                color = ArrivalBlueSoft,
                                shape = RoundedCornerShape(999.dp),
                            ) {
                                Text(
                                    text = arrivalSummary.focusTag,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    color = ArrivalBlue,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        if (arrivalSummary.focusObjective.isNotBlank()) {
                            Text(
                                text = arrivalSummary.focusObjective,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ArrivalMetric("Focus", "${arrivalSummary.durationMinutes} min")
                ArrivalMetric("Distance", "${arrivalSummary.distanceMiles.roundToInt()} mi")
                ArrivalMetric("Points", "+${arrivalSummary.pointsEarned}")
                ArrivalMetric("Seat", arrivalSummary.seat.label)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Completion status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Surface(
                        color = ArrivalBlueSoft,
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = when (arrivalSummary.status) {
                                ActiveFlightStatus.Arrived -> "Completed"
                                ActiveFlightStatus.Active -> "Active"
                                ActiveFlightStatus.Diverted -> "Diverted"
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = ArrivalBlue,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "Your flight has been saved to the logbook, points were awarded, and your current airport is now ${arrivalSummary.destination.code}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Continue Journey opens the departure board from ${arrivalSummary.destination.displayCode} so the next route starts where you landed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArrivalBlue,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        if (showReturnHomePlaceholder) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArrivalAmberSoft),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Return home planning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "You are at ${arrivalSummary.destination.code}. A dedicated return-home route builder to $homeAirportCode is planned next.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun ArrivalHeroCard(arrivalSummary: ArrivalSummary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AirportCodeBlock("FROM", arrivalSummary.origin.code)
                Text(
                    text = "TB",
                    style = MaterialTheme.typography.titleLarge,
                    color = ArrivalBlue,
                    fontWeight = FontWeight.Bold,
                )
                AirportCodeBlock("TO", arrivalSummary.destination.code)
            }
            Surface(
                color = ArrivalGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = "Flight ${arrivalSummary.flightNumber} landed",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = ArrivalGreen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
private fun ArrivalMetric(
    label: String,
    value: String,
) {
    Surface(
        color = Color.White,
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
private fun EmptyArrivalState(onContinueJourney: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArrivalBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Arrival",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Complete a focus flight to see landing details here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onContinueJourney) {
                    Text("Back to Home")
                }
            }
        }
    }
}
