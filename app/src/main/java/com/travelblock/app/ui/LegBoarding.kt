package com.travelblock.app.ui

import androidx.navigation.NavHostController
import com.travelblock.app.ui.navigation.TravelBlockDestination
import com.travelblock.app.domain.engine.ItineraryLegs
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.ui.util.AudioPlaybackSettings
import com.travelblock.app.ui.util.FlightAudioCue
import java.time.Instant
import kotlinx.coroutines.launch

/** True while the booking screen is boarding the next leg of a long haul rather than a new trip. */
internal fun TravelBlockRuntime.isBoardingNextLeg(): Boolean = state.draftFlightBooking?.continuation != null

/** The layover is over: open seat selection and a boarding pass for the next leg, like any other flight. */
internal fun beginLegBoarding(flight: ActiveFlight, runtime: TravelBlockRuntime, navController: NavHostController) {
    val draft = ItineraryLegs.nextLegDraft(flight, runtime.container.airportRepository.getAllAirports()) ?: return
    runtime.state.draftFlightBooking = draft
    navController.navigate(TravelBlockDestination.Book.route) { launchSingleTop = true }
}

/** The pass for the next leg is boarded: take off on it and go back to the cockpit. */
internal fun finishLegBoarding(pass: BoardingPass, runtime: TravelBlockRuntime, navController: NavHostController) {
    val previous = runtime.state.draftFlightBooking?.continuation ?: return
    runtime.audioCuePlayer.play(
        FlightAudioCue.FlightStarted,
        AudioPlaybackSettings(runtime.settings.soundEnabled, runtime.settings.announcementsEnabled),
    )
    val flight = ItineraryLegs.startNextLeg(previous, pass, Instant.now()).withDebugFlightDuration(runtime.settings)
    runtime.state.activeFlight = flight
    runtime.state.draftFlightBooking = null
    runtime.coroutineScope.launch {
        runtime.container.activeFlightSessionRepository.saveActiveFlight(flight)
        runtime.container.landingAlarmScheduler.schedule(flight)
        navController.popBackStack(TravelBlockDestination.Cockpit.route, false)
        createSeatUpgradeTransaction(flight, Instant.now())?.let { runtime.container.pointsRepository.insertTransaction(it) }
    }
}

/** Leaving the booking: back to the layover when boarding a next leg, otherwise to the departures board. */
internal fun cancelBooking(runtime: TravelBlockRuntime, navController: NavHostController) {
    if (runtime.isBoardingNextLeg()) {
        runtime.state.draftFlightBooking = null
        navController.popBackStack()
    } else {
        navController.popBackStack(TravelBlockDestination.Home.route, false)
    }
}
