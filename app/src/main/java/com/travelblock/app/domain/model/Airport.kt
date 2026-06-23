package com.travelblock.app.domain.model

data class Airport(
    val code: String,
    val icao: String,
    val iata: String?,
    val name: String,
    val city: String,
    val stateOrRegion: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val elevationFeet: Int?,
) {
    val displayCode: String
        get() = iata?.takeIf { it.isNotBlank() } ?: code

    val hasValidCoordinates: Boolean
        get() = latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0

    /** Requires both IATA and ICAO; used everywhere airports are loaded or searched. */
    val isUsableForTravelBlockRouting: Boolean
        get() = code.isNotBlank() &&
            !iata.isNullOrBlank() &&
            icao.isNotBlank() &&
            hasValidCoordinates
}
