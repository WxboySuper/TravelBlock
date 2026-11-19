package com.travelblock.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.travelblock.app.domain.model.ActiveFlight
import kotlinx.coroutines.launch

/** Uses a wall-clock alarm because the flight has a fixed landing timestamp. */
class LandingAlarmScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(activeFlight: ActiveFlight) {
        val alarmManager = alarmManager ?: return
        val pendingIntent = landingPendingIntent(activeFlight.startedAt.toEpochMilli())
        val triggerAt = activeFlight.nextTransitionAtEpochMillis
        try {
            if (shouldUseExactAlarm(Build.VERSION.SDK_INT, canScheduleExactAlarms(alarmManager))) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            // Permission can change between checking it and setting the alarm.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel() {
        val alarmManager = alarmManager ?: return
        val pendingIntent = landingPendingIntent(0L)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun canScheduleExactAlarms(alarmManager: AlarmManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun landingPendingIntent(startedAtMillis: Long): PendingIntent {
        val intent = Intent(context, LandingAlarmReceiver::class.java).apply {
            action = ACTION_LANDING_ALARM
            putExtra(EXTRA_STARTED_AT, startedAtMillis)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_LANDING_ALARM = "com.travelblock.app.action.LANDING_ALARM"
        const val EXTRA_STARTED_AT = "com.travelblock.app.extra.STARTED_AT"
        private const val REQUEST_CODE = 4201

        internal fun shouldUseExactAlarm(sdkInt: Int, exactAlarmPermissionGranted: Boolean): Boolean =
            sdkInt < Build.VERSION_CODES.S || exactAlarmPermissionGranted
    }
}

/** Pure decision helper kept separate so alarm selection stays covered by local unit tests. */
internal fun shouldUseExactAlarm(sdkInt: Int, exactAlarmPermissionGranted: Boolean): Boolean =
    LandingAlarmScheduler.shouldUseExactAlarm(sdkInt, exactAlarmPermissionGranted)

class LandingAlarmReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val container = com.travelblock.app.TravelBlockContainer(appContext)
                val activeFlight = container.activeFlightSessionRepository.getActiveFlight()
                val expectedStart = intent.getLongExtra(LandingAlarmScheduler.EXTRA_STARTED_AT, -1L)
                processLandingAlarm(appContext, container, activeFlight, expectedStart)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processLandingAlarm(
        context: Context,
        container: com.travelblock.app.TravelBlockContainer,
        flight: com.travelblock.app.domain.model.ActiveFlight?,
        expectedStart: Long,
    ) {
        if (flight == null || flight.startedAt.toEpochMilli() != expectedStart) return
        if (flight.status == com.travelblock.app.domain.model.ActiveFlightStatus.Layover) {
            refreshLayover(context, container, flight)
            return
        }
        if (flight.plannedArrivalAt.isAfter(java.time.Instant.now())) {
            LandingAlarmScheduler(context).schedule(flight)
            return
        }
        if (flight.isItinerary && !flight.isLastItineraryLeg) {
            persistLayover(context, container, flight)
            return
        }
        container.notificationHelper.showFlightLandedNotification(flight.flightNumber, flight.destination.displayCode)
        container.liveUpdateRefreshScheduler.cancel()
        container.liveFlightNotificationHelper.cancel()
    }

    private suspend fun persistLayover(
        context: Context,
        container: com.travelblock.app.TravelBlockContainer,
        flight: com.travelblock.app.domain.model.ActiveFlight,
    ) {
        val points = com.travelblock.app.domain.engine.FlightRewardCalculator.completedReward(flight)
        val id = java.util.UUID.nameUUIDFromBytes("${flight.flightNumber}:${flight.startedAt.toEpochMilli()}".toByteArray()).toString()
        container.flightLogRepository.insertFlightLog(flight.toCompletedLog(id, points))
        container.pointsRepository.insertTransaction(flight.toCompletedPoints(id, points))
        val minutes = flight.breakAfterLeg(flight.itineraryLegIndex)
        val layover = flight.copy(
            status = com.travelblock.app.domain.model.ActiveFlightStatus.Layover,
            itineraryPointsEarned = flight.itineraryPointsEarned + points,
            layoverEndsAtEpochMillis = flight.plannedArrivalAt.toEpochMilli() +
                java.time.Duration.ofMinutes(minutes.toLong()).toMillis(),
        )
        container.activeFlightSessionRepository.saveActiveFlight(layover)
        container.settingsRepository.setCurrentAirportCode(com.travelblock.app.data.settings.AirportCode(flight.destination.code))
        container.notificationHelper.showFlightLandedNotification(flight.flightNumber, flight.destination.displayCode)
        refreshLayover(context, container, layover)
    }

    private fun refreshLayover(
        context: Context,
        container: com.travelblock.app.TravelBlockContainer,
        flight: com.travelblock.app.domain.model.ActiveFlight,
    ) {
        container.liveFlightNotificationHelper.show(flight)
        container.liveUpdateRefreshScheduler.schedule(flight)
        if (flight.nextTransitionAtEpochMillis > System.currentTimeMillis()) {
            LandingAlarmScheduler(context).schedule(flight)
        }
    }
}

private fun com.travelblock.app.domain.model.ActiveFlight.toCompletedLog(
    id: String,
    points: Int,
) = com.travelblock.app.data.database.FlightLogEntity(
    id = id,
    flightNumber = flightNumber,
    originCode = origin.code,
    destinationCode = destination.code,
    durationMinutes = durationMinutes,
    distanceMiles = distanceMiles,
    status = com.travelblock.app.data.database.FlightLogStatus.COMPLETED.name,
    startedAt = startedAt.toEpochMilli(),
    endedAt = plannedArrivalAt.toEpochMilli(),
    seat = seat.label,
    pointsEarned = points,
    focusObjective = focusObjective,
    focusTag = focusTag,
    todoistTaskId = todoistTaskId,
    todoistTaskContent = todoistTaskContent,
)

private fun com.travelblock.app.domain.model.ActiveFlight.toCompletedPoints(
    id: String,
    points: Int,
) = com.travelblock.app.data.database.PointsTransactionEntity(
    id = id,
    amount = points,
    reason = "Completed flight",
    createdAt = plannedArrivalAt.toEpochMilli(),
    relatedFlightId = id,
)
