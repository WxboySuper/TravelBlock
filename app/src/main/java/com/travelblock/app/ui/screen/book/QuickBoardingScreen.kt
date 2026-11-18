package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.MutedText
import java.time.LocalDateTime

fun createQuickBoardingPass(
    draft: DraftFlightBooking,
    now: LocalDateTime = LocalDateTime.now(),
): BoardingPass {
    val seat = SeatOption(
        id = "10A",
        label = "10A",
        ticketClass = TicketClass.Standard,
        pointsCost = 0,
        description = "Window seat",
    )
    val flightNumber = "TB${(draft.destination.code.sumOf { it.code } + draft.durationMinutes) % 900 + 100}"
    val departure = now.plusMinutes(1)
    return BoardingPass(
        flightNumber = flightNumber,
        origin = draft.origin,
        destination = draft.destination,
        seat = seat,
        gate = "${draft.destination.code.lastOrNull() ?: 'A'}${(draft.distanceMiles.toInt() % 18) + 1}",
        boardingGroup = "Group 4",
        departureTime = departure,
        arrivalTime = departure.plusMinutes(draft.durationMinutes.toLong()),
        focusDurationMinutes = draft.durationMinutes,
        maxFlightTimeMinutes = draft.maxFlightTimeMinutes,
        distanceMiles = draft.distanceMiles,
        estimatedRewardPoints = draft.estimatedRewardPoints,
        focusObjective = "",
        focusTag = "",
    )
}

@Composable
fun QuickBoardingScreen(
    boardingPass: BoardingPass,
    onBack: () -> Unit,
    onStartFlight: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("QUICK BOARDING", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Text("A short trip through the terminal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Your flight is ready. Check in, collect your pass, then clear the gate.", style = MaterialTheme.typography.bodyMedium, color = MutedText)

        RitualMoment(number = "01", title = "Check-in complete", detail = "Flight ${boardingPass.flightNumber} · ${boardingPass.focusDurationMinutes} minute focus flight")
        RitualMoment(number = "02", title = "Boarding pass issued", detail = "Seat ${boardingPass.seat.label} · ${boardingPass.boardingGroup}")
        RitualMoment(number = "03", title = "Gate ${boardingPass.gate} cleared", detail = "${boardingPass.origin.displayCode} to ${boardingPass.destination.displayCode}")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
        ) {
            Row(
                modifier = Modifier.padding(18.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(boardingPass.origin.displayCode, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text(boardingPass.origin.city, color = MutedText)
                }
                Text("✈", style = MaterialTheme.typography.headlineMedium, color = CabinAmber)
                Column(horizontalAlignment = Alignment.End) {
                    Text(boardingPass.destination.displayCode, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text(boardingPass.destination.city, color = MutedText)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onStartFlight, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Start focus flight")
        }
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to departures")
        }
    }
}

@Composable
private fun RitualMoment(number: String, title: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(number, style = MaterialTheme.typography.titleMedium, color = CabinAmber, fontWeight = FontWeight.Black)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MutedText)
            }
        }
    }
}
