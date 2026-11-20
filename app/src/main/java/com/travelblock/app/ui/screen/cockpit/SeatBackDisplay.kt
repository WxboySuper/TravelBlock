package com.travelblock.app.ui.screen.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.travelblock.app.ui.theme.BodyFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.engine.FlightTelemetry
import com.travelblock.app.domain.engine.FlightTelemetryModel
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.TaskRefs
import com.travelblock.app.ui.components.MapOptions
import com.travelblock.app.ui.components.RouteStops
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.components.TravelMapStyle
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale

private val ScreenBackground = Color(0xFF0A0F1E)
private val ScreenText = Color(0xFFF2F5FB)
private val ScreenQuiet = Color(0xFF8E9BB8)
private const val PAGE_COUNT = 3

/**
 * The seat-back screen: swipe between the live map, the instruments (altitude, speed, outside air, heading, times at
 * both ends) and the trip (every flight of a long haul and what you are flying for).
 */
@Composable
fun SeatBackDisplay(flight: ActiveFlight, uiState: CockpitUiState, accent: Color, height: Dp) {
    val telemetry = remember(uiState.timeRemainingLabel, flight.flightNumber) { FlightTelemetryModel.at(flight, Instant.now()) }
    val pager = rememberPagerState(pageCount = { PAGE_COUNT })
    Column(Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(22.dp)).background(ScreenBackground)) {
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth()) { page ->
            when (page) {
                0 -> MapPage(flight, uiState, telemetry)
                1 -> InstrumentsPage(uiState, telemetry, accent)
                else -> TripPage(flight, accent)
            }
        }
        PageDots(pager.currentPage, accent)
    }
}

@Composable
private fun MapPage(flight: ActiveFlight, uiState: CockpitUiState, telemetry: FlightTelemetry) {
    Box(Modifier.fillMaxSize()) {
        TravelMap(
            stops = RouteStops(flight.origin.toStop(), flight.destination.toStop()),
            modifier = Modifier.fillMaxSize(),
            progress = uiState.progress.coerceIn(0f, 1f),
            options = MapOptions(style = TravelMapStyle.Night),
        )
        Column(Modifier.align(Alignment.TopStart).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MapChip("${telemetry.stage.label} / ${feet(telemetry.altitudeFeet)} ft")
            MapChip("${uiState.milesRemainingLabel} to go")
        }
    }
}

@Composable
private fun MapChip(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = ScreenText,
        modifier = Modifier.background(Color(0xB3000000), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

@Composable
private fun InstrumentsPage(uiState: CockpitUiState, telemetry: FlightTelemetry, accent: Color) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.SpaceEvenly) {
        Text(telemetry.stage.label, style = MaterialTheme.typography.titleLarge, color = accent)
        ReadingRow("Altitude" to "${feet(telemetry.altitudeFeet)} ft", "Ground speed" to "${telemetry.groundSpeedMph} mph")
        ReadingRow("Outside air" to "${telemetry.outsideTempF}°F", "Heading" to "${telemetry.headingDegrees}° ${compass(telemetry.headingDegrees)}")
        ReadingRow("Flown" to uiState.milesFlownLabel, "Local time there" to telemetry.destinationLocalTime)
    }
}

@Composable
private fun ReadingRow(left: Pair<String, String>, right: Pair<String, String>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Reading(left.first, left.second, Modifier.weight(1f))
        Reading(right.first, right.second, Modifier.weight(1f))
    }
}

@Composable
private fun Reading(label: String, value: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = ScreenQuiet)
        Text(withBodyDegrees(value), style = MaterialTheme.typography.headlineSmall, color = ScreenText, maxLines = 1)
    }
}

@Composable
private fun TripPage(flight: ActiveFlight, accent: Color) {
    val tasks = TaskRefs.decode(flight.todoistTaskId, flight.todoistTaskContent)
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (flight.isItinerary) ItineraryLegs(flight, accent) else Text("Flight ${flight.flightNumber}", style = MaterialTheme.typography.titleLarge, color = ScreenText)
        if (flight.focusObjective.isNotBlank()) Text(flight.focusObjective, style = MaterialTheme.typography.bodyMedium, color = ScreenText, maxLines = 3)
        tasks.take(3).forEach { Text(it.content, style = MaterialTheme.typography.bodySmall, color = ScreenQuiet, maxLines = 1) }
        if (flight.focusObjective.isBlank() && tasks.isEmpty()) Text("No task set for this flight.", style = MaterialTheme.typography.bodyMedium, color = ScreenQuiet)
    }
}

@Composable
private fun ItineraryLegs(flight: ActiveFlight, accent: Color) {
    val codes = flight.itineraryCodes?.split(',')?.map { it.removePrefix("K").takeIf { c -> c.length == 3 } ?: it }.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        codes.zipWithNext().forEachIndexed { index, (from, to) ->
            val done = index < flight.itineraryLegIndex
            val current = index == flight.itineraryLegIndex
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(if (done || current) accent else ScreenQuiet.copy(alpha = 0.4f), CircleShape))
                Text(
                    "$from  to  $to",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (current) ScreenText else ScreenQuiet,
                    modifier = Modifier.weight(1f),
                )
                Text(if (done) "Done" else "${flight.durationForLeg(index)} min", style = MaterialTheme.typography.labelMedium, color = ScreenQuiet)
            }
        }
    }
}

@Composable
private fun PageDots(current: Int, accent: Color) {
    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
        repeat(PAGE_COUNT) { Box(Modifier.size(if (it == current) 8.dp else 6.dp).background(if (it == current) accent else ScreenQuiet.copy(alpha = 0.5f), CircleShape)) }
    }
}

/** The wide display face has no degree sign at every width, so that one glyph is set in the body face. */
private fun withBodyDegrees(text: String): AnnotatedString = buildAnnotatedString {
    text.forEach { char ->
        if (char == '°') withStyle(SpanStyle(fontFamily = BodyFamily)) { append(char) } else append(char)
    }
}

private fun feet(value: Int): String = NumberFormat.getIntegerInstance(Locale.US).format(value)

private val Points = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

private fun compass(degrees: Int): String = Points[((degrees + 22) / 45) % Points.size]
