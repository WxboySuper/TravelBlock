package com.travelblock.app.domain.engine

import com.travelblock.app.data.repository.AirportRepository

object AirportEngineSmokeCheck {
    val KounDurationsMinutes = listOf(15, 25, 45, 60)

    fun kounReachabilitySummary(repository: AirportRepository): Map<Int, List<String>> {
        return KounDurationsMinutes.associateWith { duration ->
            repository.getReachableAirports("KOUN", duration)
                .take(5)
                .map { route ->
                    "${route.destination.code}:${route.distanceMiles.toInt()}mi:${route.availability.name}"
                }
        }
    }
}

