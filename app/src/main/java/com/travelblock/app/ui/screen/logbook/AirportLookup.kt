package com.travelblock.app.ui.screen.logbook

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.travelblock.app.data.local.AirportAssetDataSource
import com.travelblock.app.domain.model.Airport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Resolves a stored airport code (ICAO, IATA or local code) to an airport with coordinates. */
typealias AirportLookup = (String) -> Airport?

/**
 * Returns the supplied lookup, or lazily builds one from the bundled airport asset (once per process)
 * so Logbook maps work even when a caller does not hand in the app's airport repository.
 */
@Composable
internal fun rememberAirportLookup(provided: AirportLookup?): AirportLookup? {
    val context = LocalContext.current.applicationContext
    val loaded by produceState<AirportLookup?>(initialValue = null, provided == null) {
        if (provided == null) value = AirportIndex.lookup(context)
    }
    return provided ?: loaded
}

private object AirportIndex {
    @Volatile
    private var byCode: Map<String, Airport>? = null

    suspend fun lookup(context: Context): AirportLookup {
        val index = byCode ?: withContext(Dispatchers.Default) { build(context) }.also { byCode = it }
        return { code -> index[code.trim().uppercase()] }
    }

    private fun build(context: Context): Map<String, Airport> = buildMap {
        AirportAssetDataSource(context).loadAirports().forEach { airport ->
            put(airport.code.uppercase(), airport)
            put(airport.icao.uppercase(), airport)
            airport.iata?.takeIf { it.isNotBlank() }?.let { put(it.uppercase(), airport) }
        }
    }
}
