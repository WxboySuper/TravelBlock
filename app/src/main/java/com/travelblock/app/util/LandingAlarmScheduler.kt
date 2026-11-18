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
        val triggerAt = activeFlight.plannedArrivalAt.toEpochMilli()
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
                if (activeFlight == null || activeFlight.startedAt.toEpochMilli() != expectedStart) return@launch
                if (activeFlight.plannedArrivalAt.isAfter(java.time.Instant.now())) {
                    LandingAlarmScheduler(appContext).schedule(activeFlight)
                    return@launch
                }
                container.notificationHelper.showFlightLandedNotification(
                    flightNumber = activeFlight.flightNumber,
                    destinationCode = activeFlight.destination.displayCode,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
