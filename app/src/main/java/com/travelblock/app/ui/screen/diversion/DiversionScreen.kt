package com.travelblock.app.ui.screen.diversion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.DiversionSummary
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.WarmGray
import kotlin.math.roundToInt

@Composable
fun DiversionScreen(
    diversionSummary: DiversionSummary?,
    onReturnHome: () -> Unit,
    onTryAgain: () -> Unit,
    onViewLogbook: () -> Unit,
) {
    if (diversionSummary == null) {
        EmptyDiversionState(onReturnHome)
        return
    }

    Box(Modifier.fillMaxSize().background(Cream)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 250.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            LandedEarlyHeader(diversionSummary)
            DiversionMapCard(diversionSummary)
            KeptRow(diversionSummary)
            Text(
                "${diversionSummary.percentComplete}% of the way to ${diversionSummary.intendedDestination.displayCode} is still yours. " +
                    "The flight is saved in your logbook, and you are now at ${diversionSummary.diversionAirport.displayCode}. " +
                    "Rest a moment, then take off again whenever you are ready.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray,
            )
        }
        DiversionActions(onReturnHome, onTryAgain, onViewLogbook, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun LandedEarlyHeader(summary: DiversionSummary) {
    val place = summary.diversionAirport.city.ifBlank { summary.diversionAirport.displayCode }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Kicker("Change of plans", color = Marigold)
        Text("Landed early in $place", style = MaterialTheme.typography.displaySmall, color = Ink)
        Text(
            "You were headed to ${summary.intendedDestination.city.ifBlank { summary.intendedDestination.displayCode }}. " +
                "That is okay. You focused for ${summary.focusedDuration.toReadableMinutes()}.",
            style = MaterialTheme.typography.bodyLarge,
            color = WarmGray,
        )
    }
}

@Composable
private fun DiversionMapCard(summary: DiversionSummary) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TravelMap(
                origin = summary.origin.toStop(),
                destination = summary.diversionAirport.toStop(),
                progress = 1f,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${summary.origin.displayCode}  to  ${summary.diversionAirport.displayCode}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                StampPill("Flight ${summary.flightNumber}", color = Marigold)
            }
        }
    }
}

@Composable
private fun KeptRow(summary: DiversionSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Kicker("What you kept")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Kept("Focused", summary.focusedDuration.toReadableMinutes(), Modifier.weight(1f))
            Kept("Flown", "${summary.milesFlown.roundToInt()} mi", Modifier.weight(1f))
            Kept("Points", "+${summary.pointsEarned}", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Kept(label: String, value: String, modifier: Modifier = Modifier) {
    PaperCard(modifier, contentPadding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Kicker(label)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = DuskBlue)
        }
    }
}

@Composable
private fun DiversionActions(
    onReturnHome: () -> Unit,
    onTryAgain: () -> Unit,
    onViewLogbook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Cream, Cream)))
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 28.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PrimaryButton("Back to the terminal", onReturnHome)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("Try again", onTryAgain, Modifier.weight(1f))
            SecondaryButton("Logbook", onViewLogbook, Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmptyDiversionState(onReturnHome: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Cream).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("No diversions to show", style = MaterialTheme.typography.headlineMedium, color = Ink, textAlign = TextAlign.Center)
        Text(
            "If you ever land early, the details will show up here.",
            style = MaterialTheme.typography.bodyLarge,
            color = WarmGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp),
        )
        PrimaryButton("Back to Home", onReturnHome)
    }
}

private fun Airport.toStop() = MapStop(displayCode, GeoPoint(latitude, longitude))

private fun java.time.Duration.toReadableMinutes(): String {
    val totalMinutes = toMinutes().coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
