package com.travelblock.app.ui.screen.arrival

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.ArrivalSummary
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.data.todoist.TodoistRepository
import com.travelblock.app.domain.model.TaskRefs
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.RouteStops
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.scene.ArrivalScene
import com.travelblock.app.ui.scene.ArrivalLocation
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class ArrivalScreenActions(
    val continueJourney: () -> Unit,
    val viewLogbook: () -> Unit,
    val todoistRepository: TodoistRepository?,
)

@Composable
fun ArrivalScreen(
    arrivalSummary: ArrivalSummary?,
    homeAirportCode: String,
    actions: ArrivalScreenActions,
) {
    var showReturnHomePlaceholder by remember { mutableStateOf(false) }

    if (arrivalSummary == null) {
        EmptyArrivalState(actions.continueJourney)
        return
    }

    Box(Modifier.fillMaxSize().background(Cream)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            ArrivalHero(arrivalSummary.destination)
            ArrivalDetails(arrivalSummary, actions, headingHomeTo = homeAirportCode.takeIf { showReturnHomePlaceholder })
        }
        ArrivalActions(
            onContinueJourney = actions.continueJourney,
            onViewLogbook = actions.viewLogbook,
            onReturnHome = { showReturnHomePlaceholder = true },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ArrivalDetails(
    arrivalSummary: ArrivalSummary,
    actions: ArrivalScreenActions,
    /** The home airport code while the "heading home" note is showing, otherwise null. */
    headingHomeTo: String?,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 28.dp, bottom = 230.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        MetricsRow(arrivalSummary)
        RoutePostcard(arrivalSummary)
        TodoistArrivalCard(
            tasks = TaskRefs.decode(arrivalSummary.todoistTaskId, arrivalSummary.todoistTaskContent).map { TodoistArrivalTask(it.id, it.content) },
            repository = actions.todoistRepository,
        )
        if (arrivalSummary.focusObjective.isNotBlank() || arrivalSummary.focusTag.isNotBlank()) {
            FocusRecap(arrivalSummary.focusTag, arrivalSummary.focusObjective)
        }
        Text(
            "Saved to your logbook. Your points are added, and you are now at ${arrivalSummary.destination.displayCode}. " +
                "Your next departure board starts right here.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmGray,
        )
        headingHomeTo?.let { ReturnHomeNote(arrivalSummary.destination.displayCode, it) }
    }
}

/** The sky over the arrival airport, edge to edge, with the city as the headline and the passport stamp pressed on it. */
@Composable
private fun ArrivalHero(destination: Airport) {
    val placeLine = listOfNotNull(
        destination.displayCode,
        destination.stateOrRegion?.takeIf { it.isNotBlank() },
    ).joinToString(" / ")
    Box(Modifier.fillMaxWidth().height(340.dp)) {
        ArrivalScene(
            ArrivalLocation(destination.displayCode, destination.longitude),
            Modifier.fillMaxSize(),
            corner = 0.dp,
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC14131A)))))
        Column(
            Modifier.align(Alignment.BottomStart).padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Kicker("You have landed", color = Color.White.copy(alpha = 0.92f))
            Text(destination.city.ifBlank { destination.displayCode }, style = MaterialTheme.typography.displayMedium, color = Color.White)
            if (placeLine.isNotBlank()) Text(placeLine, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
        }
        PassportStamp(destination.displayCode, Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 28.dp, top = 28.dp))
    }
}

/** A rubber-stamp style badge, slightly tilted like it was pressed by hand. */
@Composable
private fun PassportStamp(code: String, modifier: Modifier = Modifier) {
    val date = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d yyyy")).uppercase() }
    Column(
        modifier
            .rotate(-9f)
            .background(Color.White.copy(alpha = 0.78f), RoundedCornerShape(14.dp))
            .border(BorderStroke(3.dp, Terracotta.copy(alpha = 0.85f)), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ARRIVED", style = MaterialTheme.typography.labelSmall, color = Terracotta)
        Text(code, style = MaterialTheme.typography.headlineMedium, color = Terracotta)
        Text(date, style = MaterialTheme.typography.labelSmall, color = Terracotta)
    }
}

/** The route on a flat full-width map; the flight number sits underneath instead of in a card. */
@Composable
private fun RoutePostcard(summary: ArrivalSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TravelMap(
            stops = RouteStops(summary.origin.toStop(), summary.destination.toStop()),
            progress = 1f,
            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${summary.origin.displayCode}  to  ${summary.destination.displayCode}", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Text("Flight ${summary.flightNumber}", style = MaterialTheme.typography.labelLarge, color = WarmGray)
        }
    }
}

/** Three big numbers side by side, no boxes: what this flight was worth. */
@Composable
private fun MetricsRow(summary: ArrivalSummary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Metric("Focused", "${summary.durationMinutes} min", Modifier.weight(1f))
        Metric("Distance", "${summary.distanceMiles.roundToInt()} mi", Modifier.weight(1f))
        Metric("Points", "+${summary.pointsEarned}", Modifier.weight(1f), highlight = true)
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Kicker(label)
        Text(value, style = MaterialTheme.typography.headlineMedium, color = if (highlight) DuskBlue else Ink, maxLines = 1)
    }
}

@Composable
private fun FocusRecap(tag: String, objective: String) {
    PaperCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Kicker("What you worked on")
            if (tag.isNotBlank()) StampPill(tag, color = DuskBlue)
            if (objective.isNotBlank()) Text(objective, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
    }
}

@Composable
private fun ReturnHomeNote(here: String, homeAirportCode: String) {
    PaperCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Heading home", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                "You are at $here. A route builder that takes you back to $homeAirportCode is coming soon.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray,
            )
        }
    }
}

@Composable
private fun ArrivalActions(
    onContinueJourney: () -> Unit,
    onViewLogbook: () -> Unit,
    onReturnHome: () -> Unit,
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
        PrimaryButton("Back to the terminal", onContinueJourney)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("Logbook", onViewLogbook, Modifier.weight(1f))
            SecondaryButton("Head home", onReturnHome, Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmptyArrivalState(onContinueJourney: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Cream).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Nothing has landed yet", style = MaterialTheme.typography.headlineMedium, color = Ink, textAlign = TextAlign.Center)
        Text(
            "Finish a focus flight and your arrival card will appear here.",
            style = MaterialTheme.typography.bodyLarge,
            color = WarmGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp),
        )
        PrimaryButton("Back to Home", onContinueJourney)
    }
}

private fun Airport.toStop() = MapStop(displayCode, GeoPoint(latitude, longitude))
