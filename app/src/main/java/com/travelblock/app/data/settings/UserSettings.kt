package com.travelblock.app.data.settings

data class UserSettings(
    val homeAirportCode: String = DefaultAirportCode,
    val currentAirportCode: String = DefaultAirportCode,
    val selectedTheme: String = "system",
    val soundEnabled: Boolean = true,
    val announcementsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val onboardingComplete: Boolean = false,
    val debugShortFlightsEnabled: Boolean = false,
    val debugFlightDurationSeconds: Int = 15,
    val equippedBoardingPassThemeId: String = "",
    val equippedAircraftIconId: String = "",
) {
    companion object {
        const val DefaultAirportCode = "KOUN"
    }
}
