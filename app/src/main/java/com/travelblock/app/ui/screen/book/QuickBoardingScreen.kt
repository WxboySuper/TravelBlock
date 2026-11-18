package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.scene.JetBridgeScene
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.WarmGray
import kotlinx.coroutines.delay
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
    var done by remember(boardingPass.flightNumber) { mutableIntStateOf(0) }
    LaunchedEffect(boardingPass.flightNumber) {
        repeat(3) {
            delay(650)
            done += 1
        }
    }
    val cleared = done >= 3
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Kicker("Fast track", color = Sage)
        Text("Straight to your gate", style = MaterialTheme.typography.headlineLarge, color = Ink)
        JetBridgeScene()
        QuickMoment("Checked in", "Flight ${boardingPass.flightNumber} / ${boardingPass.focusDurationMinutes} minute focus flight", done >= 1)
        QuickMoment("Boarding pass issued", "Seat ${boardingPass.seat.label} / ${boardingPass.boardingGroup}", done >= 2)
        QuickMoment("Gate ${boardingPass.gate} cleared", "${boardingPass.origin.displayCode} to ${boardingPass.destination.displayCode}", done >= 3)
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryButton(if (cleared) "Start focus flight" else "Walking to the gate...", onClick = onStartFlight, enabled = cleared)
        SecondaryButton("Back to departures", onClick = onBack)
    }
}

/** One stop on the walk, ticking from an empty circle to a check once it is done. */
@Composable
private fun QuickMoment(title: String, detail: String, done: Boolean) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(26.dp).clip(CircleShape).background(if (done) Sage else Sand),
                contentAlignment = Alignment.Center,
            ) {
                if (done) Icon(Icons.Rounded.Check, contentDescription = null, tint = Cream, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = if (done) Ink else WarmGray)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
        }
    }
}
