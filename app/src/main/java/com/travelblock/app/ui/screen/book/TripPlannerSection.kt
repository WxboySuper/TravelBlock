package com.travelblock.app.ui.screen.book

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.travelblock.app.domain.engine.HubRouting
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.ui.theme.DuskBlue

/** Trips shorter than this are quick hops that fly direct unless the traveller adds a hub. */
private const val QUICK_HOP_MILES = 60.0

/** The trip card plus the hub choices for the picked destination, with a sensible default already applied. */
@Composable
internal fun TripPlannerSection(uiState: BookingUiState, origin: Airport, airports: List<Airport>, actions: TripPlannerActions) {
    val hubs = remember(origin.code, uiState.destination?.code, airports) {
        uiState.destination?.let { HubRouting.connections(origin, it, airports) }.orEmpty()
    }
    // Smart default: a trip long enough to matter connects through its nearest hub unless you say otherwise.
    LaunchedEffect(hubs, uiState.hubChosen) {
        if (shouldDefaultToHub(uiState, hubs.isNotEmpty())) actions.onHub(hubs.first())
    }
    TripPlannerCard(
        TripPlannerState(uiState.tripPreset, uiState.sessionPlan, uiState.destination, airports, HubOption(hubs, uiState.hub)),
        actions,
    )
}

private fun shouldDefaultToHub(uiState: BookingUiState, hasHubs: Boolean): Boolean {
    val plain = uiState.tripPreset == null && uiState.sessionPlan == null
    return hasHubs && plain && !uiState.hubChosen && uiState.distanceMiles > QUICK_HOP_MILES
}

/** The whole route of a trip with stops, so the first flight's docket is never mistaken for the whole trip. */
@Composable
internal fun TripRouteLine(stops: List<Airport>) {
    if (stops.size < 3) return
    Text(
        "Trip: ${stops.joinToString("  to  ") { it.displayCode }}  (${stops.size - 1} flights)",
        style = MaterialTheme.typography.labelLarge,
        color = DuskBlue,
    )
}
