package com.travelblock.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.travelblock.app.domain.model.ActiveFlight

class LiveUpdateRefreshScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(flight: ActiveFlight, nowMillis: Long = System.currentTimeMillis()) {
        val manager = alarmManager ?: return
        val untilTransition = flight.nextTransitionAtEpochMillis - nowMillis
        val refreshDelay = if (untilTransition <= 0L) REFRESH_INTERVAL_MILLIS
        else minOf(REFRESH_INTERVAL_MILLIS, untilTransition)
        val triggerAt = nowMillis + refreshDelay
        val pending = pendingIntent(flight.startedAt.toEpochMilli())
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } catch (_: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel() {
        val pending = pendingIntent(0L)
        alarmManager?.cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(startMillis: Long): PendingIntent {
        val intent = Intent(context, LiveUpdateRefreshReceiver::class.java).apply {
            action = ACTION_REFRESH_LIVE_UPDATE
            putExtra(EXTRA_STARTED_AT, startMillis)
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val ACTION_REFRESH_LIVE_UPDATE = "com.travelblock.app.action.REFRESH_LIVE_UPDATE"
        const val EXTRA_STARTED_AT = "com.travelblock.app.extra.LIVE_STARTED_AT"
        private const val REQUEST_CODE = 4202
        private const val REFRESH_INTERVAL_MILLIS = 60_000L
    }
}
