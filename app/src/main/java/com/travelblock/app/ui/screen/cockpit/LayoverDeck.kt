package com.travelblock.app.ui.screen.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.scene.FlapText
import com.travelblock.app.ui.scene.TerminalWindows
import com.travelblock.app.ui.scene.localSolarHour
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray
import java.time.Instant

private const val LAYOVER_DONE = "00:00"

/**
 * The break between two legs of a long haul: the connecting airport through the terminal windows, a departures
 * board style countdown, and where the trip stands. Skippable, and boarding opens when the countdown ends.
 */
@Composable
fun LayoverDeck(
    flight: ActiveFlight,
    uiState: CockpitUiState,
    onSkip: () -> Unit,
    onBoard: () -> Unit,
) {
    val hour = remember(flight.destination.code) { localSolarHour(Instant.now(), flight.destination.longitude) }
    val ready = uiState.timeRemainingLabel == LAYOVER_DONE
    Column(Modifier.fillMaxSize().background(Cream)) {
        Box(Modifier.fillMaxWidth().height(250.dp)) {
            TerminalWindows(hour, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x33000000), Color(0xCC14131A)))))
            LayoverHeader(flight, Modifier.statusBarsPadding())
        }
        Column(
            Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CountdownBoard(uiState.timeRemainingLabel, ready)
            TripProgress(flight)
            NextLegCard(flight)
            StretchNote()
            Spacer(Modifier.weight(1f))
            PrimaryButton(if (ready) "Board next leg" else "Boarding opens at 00:00", onClick = onBoard, enabled = ready)
            if (!ready) SecondaryButton("Skip layover", onClick = onSkip)
        }
    }
}

@Composable
private fun LayoverHeader(flight: ActiveFlight, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 100.dp)) {
        Kicker("${flight.itineraryLabel ?: "Long haul"} / layover", color = Color.White.copy(alpha = 0.92f))
        Text("Layover in ${flight.destination.city.ifBlank { flight.destination.displayCode }}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
    }
}

@Composable
private fun CountdownBoard(timeLabel: String, ready: Boolean) {
    Column(
        Modifier.fillMaxWidth().background(Color(0xFF1C1B22), RoundedCornerShape(22.dp)).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(if (ready) "NOW BOARDING" else "NEXT LEG BOARDS IN", style = MaterialTheme.typography.labelLarge, color = com.travelblock.app.ui.theme.Lamp)
        FlapText(timeLabel, width = 44.dp, height = 64.dp, fontSize = 38)
    }
}

@Composable
private fun TripProgress(flight: ActiveFlight) {
    val done = flight.itineraryLegIndex + 1
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Kicker("Leg $done of ${flight.itineraryLegCount} complete", color = WarmGray)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(flight.itineraryLegCount) { index ->
                val reached = index < done
                Box(Modifier.size(if (reached) 14.dp else 10.dp).background(if (reached) Terracotta else Sand, CircleShape))
                if (index < flight.itineraryLegCount - 1) {
                    Box(Modifier.weight(1f).height(2.dp).background(if (index + 1 < done) Terracotta else Sand))
                }
            }
        }
    }
}

@Composable
private fun NextLegCard(flight: ActiveFlight) {
    Row(
        Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(18.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Kicker("Next leg", color = WarmGray)
            Text(
                "${flight.destination.displayCode}  to  ${nextLegCode(flight)}",
                style = MaterialTheme.typography.titleLarge, color = Ink,
            )
        }
        Text("Seat ${flight.seat.label}", style = MaterialTheme.typography.labelLarge, color = DuskBlue, textAlign = TextAlign.End)
    }
}

/** The next stop as travellers know it: the planner stores ICAO codes, so US airports lose their leading K. */
internal fun nextLegCode(flight: ActiveFlight): String {
    val code = flight.itineraryCodes?.split(',')?.getOrNull(flight.itineraryLegIndex + 2) ?: return "next airport"
    return if (code.length == 4 && code.startsWith("K")) code.drop(1) else code
}

@Composable
private fun StretchNote() {
    Text(
        "Stretch your legs, sip some water. Your focus timer is paused until you board.",
        style = MaterialTheme.typography.bodyMedium,
        color = WarmGray,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}
