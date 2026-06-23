package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport

object AirportSearchEngine {
    fun search(
        airports: List<Airport>,
        query: String,
        limit: Int = 30,
    ): List<Airport> {
        val terms = query.trim()
            .lowercase()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val usableAirports = airports.filter { it.isUsableForTravelBlockRouting }
        if (terms.isEmpty()) return usableAirports.sortedWith(defaultSort()).take(limit)

        return usableAirports.asSequence()
            .mapNotNull { airport ->
                val searchableText = airport.searchText()
                if (terms.all { it in searchableText }) {
                    airport to score(airport, terms)
                } else {
                    null
                }
            }
            .sortedWith(
                compareByDescending<Pair<Airport, Int>> { it.second }
                    .thenBy { it.first.code },
            )
            .map { it.first }
            .take(limit)
            .toList()
    }

    private fun score(airport: Airport, terms: List<String>): Int {
        return terms.sumOf { term ->
            when {
                airport.code.lowercase() == term -> 120
                airport.icao.lowercase() == term -> 110
                airport.iata?.lowercase() == term -> 100
                airport.code.lowercase().startsWith(term) -> 80
                airport.icao.lowercase().startsWith(term) -> 75
                airport.iata?.lowercase()?.startsWith(term) == true -> 70
                airport.city.lowercase().contains(term) -> 45
                airport.name.lowercase().contains(term) -> 40
                airport.stateOrRegion?.lowercase()?.contains(term) == true -> 25
                airport.country.lowercase().contains(term) -> 15
                else -> 1
            }
        } + if (airport.icao.isNotBlank() && !airport.iata.isNullOrBlank()) 10 else 0
    }

    private fun Airport.searchText(): String {
        return listOfNotNull(
            code,
            icao,
            iata,
            name,
            city,
            stateOrRegion,
            country,
        ).joinToString(" ").lowercase()
    }

    private fun defaultSort(): Comparator<Airport> {
        return compareByDescending<Airport> {
            if (it.icao.isNotBlank() && !it.iata.isNullOrBlank()) 1 else 0
        }.thenBy { it.code }
    }
}
