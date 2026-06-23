package com.travelblock.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TravelBlockDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : TravelBlockDestination("home", "Home", Icons.Outlined.Home)
    data object Book : TravelBlockDestination("book", "Book", Icons.Outlined.Home)
    data object Arrival : TravelBlockDestination("arrival", "Arrival", Icons.Outlined.AirplanemodeActive)
    data object Diversion : TravelBlockDestination("diversion", "Diversion", Icons.Outlined.AirplanemodeActive)
    data object Cockpit : TravelBlockDestination("cockpit", "Cockpit", Icons.Outlined.AirplanemodeActive)
    data object Logbook : TravelBlockDestination("logbook", "Logbook", Icons.AutoMirrored.Outlined.MenuBook)
    data object LogbookDetail : TravelBlockDestination("logbook/{flightId}", "Flight Detail", Icons.AutoMirrored.Outlined.MenuBook) {
        const val FlightIdArg = "flightId"

        fun createRoute(flightId: String): String = "logbook/$flightId"
    }
    data object Store : TravelBlockDestination("store", "Store", Icons.Outlined.LocalMall)
    data object Settings : TravelBlockDestination("settings", "Settings", Icons.Outlined.Settings)

    companion object {
        val normalBottomTabs = listOf(Home, Logbook, Store, Settings)
        val activeFlightBottomTabs = listOf(Cockpit, Logbook, Store, Settings)
    }
}
