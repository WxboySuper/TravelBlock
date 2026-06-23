package com.travelblock.app.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelBlockNavigationTest {
    @Test
    fun normalBottomTabsHideCockpit() {
        val routes = TravelBlockDestination.normalBottomTabs.map { it.route }
        assertFalse(routes.contains(TravelBlockDestination.Cockpit.route))
        assertFalse(routes.contains(TravelBlockDestination.Book.route))
    }

    @Test
    fun activeFlightBottomTabsShowCockpitAndHideBook() {
        val routes = TravelBlockDestination.activeFlightBottomTabs.map { it.route }
        assertTrue(routes.contains(TravelBlockDestination.Cockpit.route))
        assertFalse(routes.contains(TravelBlockDestination.Home.route))
        assertFalse(routes.contains(TravelBlockDestination.Book.route))
    }
}
