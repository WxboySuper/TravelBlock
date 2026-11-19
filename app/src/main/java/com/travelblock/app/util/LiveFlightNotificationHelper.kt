package com.travelblock.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.content.ContextCompat
import com.travelblock.app.MainActivity
import com.travelblock.app.R
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.FlightLivePhase
import com.travelblock.app.domain.model.FlightLiveUpdate
import com.travelblock.app.domain.model.FlightLiveUpdateModel
import java.time.Instant

class LiveFlightNotificationHelper(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "Flight status", NotificationManager.IMPORTANCE_LOW)
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun show(flight: ActiveFlight, now: Instant = Instant.now()) {
        if (!canPost()) return
        createChannel()
        val model = FlightLiveUpdateModel.create(flight, now)
        if (model.phase == FlightLivePhase.ARRIVED) {
            cancel()
            return
        }
        val notification = buildNotification(flight, model)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun canPost(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun buildNotification(flight: ActiveFlight, model: FlightLiveUpdate): android.app.Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_plane)
            .setContentTitle(model.title)
            .setContentText(model.statusText)
            .setSubText("Seat ${flight.seat.label}")
            .setOngoing(true)
            .setWhen(flight.plannedArrivalAt.toEpochMilli())
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setContentIntent(contentIntent())
            .setOnlyAlertOnce(true)
            .setShortCriticalText(model.shortChipText)
        if (isPromotionAvailable()) builder.setStyle(progressStyle(model)).setRequestPromotedOngoing(true)
        else builder.setProgress(model.segments.total, model.progressValue, false)
        return builder.build()
    }

    private fun progressStyle(model: FlightLiveUpdate): NotificationCompat.ProgressStyle {
        val segment = NotificationCompat.ProgressStyle.Segment(model.segments.takeoff).setColor(TERRACOTTA)
        val cruise = NotificationCompat.ProgressStyle.Segment(model.segments.cruise).setColor(TERRACOTTA)
        val descent = NotificationCompat.ProgressStyle.Segment(model.segments.descent).setColor(TERRACOTTA)
        return NotificationCompat.ProgressStyle()
            .setProgress(model.progressValue)
            .setProgressSegments(listOf(segment, cruise, descent))
            .setProgressPoints(listOf(
                NotificationCompat.ProgressStyle.Point(model.segments.takeoff).setColor(SAND),
                NotificationCompat.ProgressStyle.Point(model.segments.takeoff + model.segments.cruise).setColor(SAND),
            ))
            .setProgressTrackerIcon(IconCompat.createWithResource(context, R.drawable.ic_plane))
            .setStyledByProgress(true)
    }

    private fun isPromotionAvailable(): Boolean = Build.VERSION.SDK_INT >= 36 &&
        context.getSystemService(NotificationManager::class.java)?.canPostPromotedNotifications() == true

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val CHANNEL_ID = "travelblock_live_flight"
        const val NOTIFICATION_ID = 1002
        private const val TERRACOTTA = 0xFFCF6F4A.toInt()
        private const val SAND = 0xFFE8DFCF.toInt()
    }
}
