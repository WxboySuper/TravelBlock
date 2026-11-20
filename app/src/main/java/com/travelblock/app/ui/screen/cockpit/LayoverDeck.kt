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
import com.travelblock.app.ui.theme.Sand
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
            Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CountdownBoard(uiState.timeRemainingLabel, ready)
            TripProgress(flight)
            NextLeg(flight)
            Spacer(Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton(if (ready) "Board next leg" else "Boarding opens at 00:00", onClick = onBoard, enabled = ready)
                if (!ready) SecondaryButton("Skip layover", onClick = onSkip)
            }
        }
    }
}

@Composable
private fun LayoverHeader(flight: ActiveFlight, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 96.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Kicker("${flight.itineraryLabel ?: "Long haul"} / layover", color = Color.White.copy(alpha = 0.92f))
        Text(
            flight.destination.city.ifBlank { flight.destination.displayCode },
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
        )
        Text(
            "Layover at ${flight.destination.displayCode}",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

/** One big departures-board panel: the countdown is the thing on this screen. */
@Composable
private fun CountdownBoard(timeLabel: String, ready: Boolean) {
    Column(
        Modifier.fillMaxWidth().background(Color(0xFF1C1B22), RoundedCornerShape(28.dp)).padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(if (ready) "NOW BOARDING" else "NEXT LEG BOARDS IN", style = MaterialTheme.typography.labelLarge, color = com.travelblock.app.ui.theme.Lamp)
        FlapText(timeLabel, width = 52.dp, height = 78.dp, fontSize = 46)
    }
}

/** The whole trip as a line of airport codes: where you have been, where you are, what is left. */
@Composable
private fun TripProgress(flight: ActiveFlight) {
    val codes = tripCodes(flight)
    val here = flight.itineraryLegIndex + 1
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Kicker("Leg ${here} of ${flight.itineraryLegCount} complete", color = WarmGray)
        Row(verticalAlignment = Alignment.Top) {
            codes.forEachIndexed { index, code ->
                val reached = index <= here
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(if (index == here) 18.dp else 12.dp).background(if (reached) DuskBlue else Sand, CircleShape))
                    }
                    Text(code, style = MaterialTheme.typography.labelLarge, color = if (index == here) Ink else WarmGray)
                }
                if (index < codes.lastIndex) {
                    Box(Modifier.weight(1f).height(18.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.fillMaxWidth().height(3.dp).background(if (index < here) DuskBlue else Sand))
                    }
                }
            }
        }
    }
}

@Composable
private fun NextLeg(flight: ActiveFlight) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column {
            Kicker("Next flight", color = WarmGray)
            Text(
                "${flight.destination.displayCode}  to  ${nextLegCode(flight)}",
                style = MaterialTheme.typography.headlineLarge, color = Ink,
            )
        }
        Text("Seat ${flight.seat.label}", style = MaterialTheme.typography.titleMedium, color = DuskBlue, textAlign = TextAlign.End)
    }
    Text(
        "Stretch your legs, sip some water. Your focus timer is paused until you board.",
        style = MaterialTheme.typography.bodyMedium,
        color = WarmGray,
    )
}

/** Airport codes along the trip in order, as travellers know them (US airports lose the planner's leading K). */
internal fun tripCodes(flight: ActiveFlight): List<String> =
    flight.itineraryCodes?.split(',').orEmpty().map { if (it.length == 4 && it.startsWith("K")) it.drop(1) else it }

/** The next stop as travellers know it: the planner stores ICAO codes, so US airports lose their leading K. */
internal fun nextLegCode(flight: ActiveFlight): String {
    val code = flight.itineraryCodes?.split(',')?.getOrNull(flight.itineraryLegIndex + 2) ?: return "next airport"
    return if (code.length == 4 && code.startsWith("K")) code.drop(1) else code
}
