package com.travelblock.app.data.local

import android.content.Context
import com.travelblock.app.domain.model.Airport

class AirportAssetDataSource(
    private val context: Context,
    private val parser: AirportJsonParser = AirportJsonParser(),
) {
    fun loadAirports(): List<Airport> {
        return runCatching {
            context.assets.open(AirportAssetName).bufferedReader().use { reader ->
                parser.parse(reader.readText())
            }
        }.getOrElse { emptyList() }
    }

    companion object {
        const val AirportAssetName = "airports.json"
    }
}

