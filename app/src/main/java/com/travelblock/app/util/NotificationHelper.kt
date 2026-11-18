package com.travelblock.app.util

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.travelblock.app.MainActivity
import com.travelblock.app.R

/**
 * Notification display for focus-flight landing alerts.
 */
class NotificationHelper(
    private val context: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun hasPostNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun showFlightLandedNotification(
        flightNumber: String,
        destinationCode: String,
    ) {
        if (!hasPostNotificationPermission()) return

        ensureChannel()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_ARRIVAL, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            LANDED_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("You have landed")
            .setContentText("Flight $flightNumber has landed at $destinationCode")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(LANDED_NOTIFICATION_ID, notification)
    }

    fun cancelFlightLandedNotification() {
        notificationManager.cancel(LANDED_NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_flights),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_flights_description)
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "travelblock_flights"
        const val LANDED_NOTIFICATION_ID = 1001
        const val EXTRA_OPEN_ARRIVAL = "com.travelblock.app.extra.OPEN_ARRIVAL"
    }
}
