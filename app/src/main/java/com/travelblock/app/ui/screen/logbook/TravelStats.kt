package com.travelblock.app.ui.screen.logbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.theme.WarmGray

/** The places and habits behind the logbook: how far, how wide, and what you were working on. */
data class TravelStats(
    val airports: Int = 0,
    val countries: Int = 0,
    val longestMiles: Int = 0,
    val longestRoute: String = "",
    val favouriteAirport: String? = null,
    val favouriteAirportVisits: Int = 0,
    val topFocusTag: String? = null,
)

/** Landed flights only, matching the map. Airports and countries count both ends of every leg. */
fun travelStatsFor(entries: List<LogbookEntryUiModel>, lookup: AirportLookup?): TravelStats {
    val landed = entries.filter { it.isCompleted }
    val codes = landed.flatMap { listOf(it.originCode, it.destinationCode) }.filter { it.isNotBlank() }
    val longest = landed.maxByOrNull { it.distanceMiles }
    val favourite = landed.groupingBy { it.destinationCode }.eachCount().filterKeys { it.isNotBlank() }.maxByOrNull { it.value }
    return TravelStats(
        airports = codes.toSet().size,
        countries = codes.mapNotNull { lookup?.invoke(it)?.country }.filter { it.isNotBlank() }.toSet().size,
        longestMiles = longest?.distanceMiles?.toInt() ?: 0,
        longestRoute = longest?.let { "${it.originCode} to ${it.destinationCode}" }.orEmpty(),
        favouriteAirport = favourite?.key,
        favouriteAirportVisits = favourite?.value ?: 0,
        topFocusTag = landed.map { it.focusTag }.filter { it.isNotBlank() }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key,
    )
}

/** The airport code as travellers know it: the planner stores ICAO codes, so US airports lose their leading K. */
internal fun String.asAirportCode(): String = if (length == 4 && startsWith("K")) drop(1) else this

@Composable
internal fun TravelStatsCard(stats: TravelStats) {
    val favourite = stats.favouriteAirport?.asAirportCode()
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Kicker("Travel stats")
            TotalsRow("Airports" to stats.airports.toString(), "Countries" to stats.countries.toString(), "Longest" to "${stats.longestMiles} mi")
            if (stats.longestRoute.isNotBlank()) {
                Text(
                    "Longest flight: ${stats.longestRoute.split(" to ").joinToString(" to ") { it.asAirportCode() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGray,
                )
            }
            favourite?.let {
                Text("Most landings at $it: ${stats.favouriteAirportVisits}", style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
            stats.topFocusTag?.let { Text("Most flown for: $it", style = MaterialTheme.typography.bodySmall, color = WarmGray) }
        }
    }
}
