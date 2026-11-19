package com.travelblock.app.ui.screen.logbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MetricColumn
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TravelStatsCard(stats: TravelStats) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Kicker("Travel stats")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricColumn("Airports", stats.airports.toString())
                MetricColumn("Countries", stats.countries.toString())
                MetricColumn("Longest", "${stats.longestMiles} mi")
                stats.favouriteAirport?.let { MetricColumn("Favourite", it) }
            }
            if (stats.longestRoute.isNotBlank()) {
                Text("Longest flight: ${stats.longestRoute}", style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
            stats.favouriteAirport?.let {
                Text("Most landings at $it: ${stats.favouriteAirportVisits}", style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
            stats.topFocusTag?.let { Text("Most flown for: $it", style = MaterialTheme.typography.bodySmall, color = WarmGray) }
        }
    }
}
