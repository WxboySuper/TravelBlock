package com.travelblock.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.travelblock.app.TravelBlockContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class LiveUpdateRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                refresh(appContext, intent.getLongExtra(LiveUpdateRefreshScheduler.EXTRA_STARTED_AT, -1L))
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun refresh(context: Context, expectedStart: Long) {
        val container = TravelBlockContainer(context)
        if (!container.liveFlightNotificationHelper.canPostNotifications()) {
            LiveUpdateRefreshScheduler(context).cancel()
            return
        }
        val flight = container.activeFlightSessionRepository.getActiveFlight()
        if (flight == null || flight.startedAt.toEpochMilli() != expectedStart) return
        if (!flight.plannedArrivalAt.isAfter(Instant.now())) {
            LiveUpdateRefreshScheduler(context).cancel()
            LiveFlightNotificationHelper(context).cancel()
            return
        }
        LiveFlightNotificationHelper(context).show(flight)
        LiveUpdateRefreshScheduler(context).schedule(flight)
    }
}
