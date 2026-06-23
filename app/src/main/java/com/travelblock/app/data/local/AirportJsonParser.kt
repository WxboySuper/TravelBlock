package com.travelblock.app.data.local

import com.travelblock.app.domain.model.Airport
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AirportJsonParser {
    fun parse(jsonText: String): List<Airport> {
        if (jsonText.isBlank()) return emptyList()

        return runCatching {
            Json.parseToJsonElement(jsonText).jsonObject.mapNotNull { (key, value) ->
                parseAirport(code = key, element = value)
            }
        }.getOrElse { emptyList() }
    }

    private fun parseAirport(code: String, element: JsonElement): Airport? {
        val airportJson = element as? JsonObject ?: return null
        val latitude = airportJson.double("lat") ?: return null
        val longitude = airportJson.double("lon") ?: return null
        val name = airportJson.text("name") ?: return null
        val city = airportJson.text("city") ?: ""
        val country = airportJson.text("country") ?: ""
        val icao = airportJson.text("icao")?.ifBlank { null }
        val iata = airportJson.text("iata")?.ifBlank { null }

        return Airport(
            code = code.uppercase(),
            icao = icao?.uppercase().orEmpty(),
            iata = iata?.uppercase(),
            name = name,
            city = city,
            stateOrRegion = airportJson.text("state")?.ifBlank { null },
            country = country,
            latitude = latitude,
            longitude = longitude,
            elevationFeet = airportJson.int("elevation"),
        )
    }

    private fun JsonObject.text(name: String): String? {
        return this[name]?.jsonPrimitive?.content
    }

    private fun JsonObject.double(name: String): Double? {
        return this[name]?.jsonPrimitive?.doubleOrNull
    }

    private fun JsonObject.int(name: String): Int? {
        return this[name]?.jsonPrimitive?.intOrNull
    }
}
