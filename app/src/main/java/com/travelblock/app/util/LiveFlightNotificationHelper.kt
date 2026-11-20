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
import com.travelblock.app.domain.model.TripBar
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
        if (!canPostNotifications()) return
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

    fun canPostNotifications(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun buildNotification(flight: ActiveFlight, model: FlightLiveUpdate): android.app.Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_plane)
            .setContentTitle(model.title)
            .setContentText(contentLine(model))
            .setSubText(flight.liveLegLabel())
            .setColor(ACCENT)
            .setColorized(false)
            .setLargeIcon(LiveRouteArt.render(flight, routeProgress(flight, model)))
            .setOngoing(true)
            .setWhen(flight.nextTransitionAtEpochMillis)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setContentIntent(contentIntent())
            .setOnlyAlertOnce(true)
            .setShortCriticalText(model.shortChipText)
        if (isPromotionAvailable()) builder.setStyle(progressStyle(model)).setRequestPromotedOngoing(true)
        else if (model.trip != null) builder.setProgress(model.trip.totalMinutes, model.trip.progressMinutes, false)
        else if (model.phase != FlightLivePhase.LAYOVER) builder.setProgress(model.segments.total, model.progressValue, false)
        return builder.build()
    }

    /** The one line under the title: the countdown at a layover, otherwise the instruments. */
    private fun contentLine(model: FlightLiveUpdate): String =
        if (model.phase == FlightLivePhase.LAYOVER) model.statusText else model.detailText.ifBlank { model.statusText }

    private fun progressStyle(model: FlightLiveUpdate): NotificationCompat.ProgressStyle {
        val trip = model.trip
        return if (trip != null) tripStyle(trip, model) else flightStyle(model)
    }

    /** One flight: climb, cruise and descent in their own colours, with the plane pitched to match the stage. */
    private fun flightStyle(model: FlightLiveUpdate): NotificationCompat.ProgressStyle {
        val segments = listOf(
            NotificationCompat.ProgressStyle.Segment(model.segments.takeoff).setColor(MIST),
            NotificationCompat.ProgressStyle.Segment(model.segments.cruise).setColor(ACCENT),
            NotificationCompat.ProgressStyle.Segment(model.segments.descent).setColor(SIGNAL),
        )
        return endpoints(NotificationCompat.ProgressStyle(), model)
            .setProgress(model.progressValue)
            .setProgressSegments(segments)
            .setProgressPoints(listOf(
                NotificationCompat.ProgressStyle.Point(model.segments.takeoff).setColor(WHITE),
                NotificationCompat.ProgressStyle.Point(model.segments.takeoff + model.segments.cruise).setColor(WHITE),
            ))
            .setStyledByProgress(true)
    }

    /** A long haul: every flight is a cobalt segment, every layover a pale one, with a stop marker between them. */
    private fun tripStyle(trip: TripBar, model: FlightLiveUpdate): NotificationCompat.ProgressStyle {
        val flights = trip.flightMinutes.size
        val segments = trip.segments.mapIndexed { index, minutes ->
            NotificationCompat.ProgressStyle.Segment(minutes.coerceAtLeast(1)).setColor(if (index % 2 == 0) ACCENT else MIST)
        }
        val stops = trip.segments.runningFold(0) { total, minutes -> total + minutes }.drop(1).dropLast(1)
            .filterIndexed { index, _ -> index % 2 == 0 && index / 2 < flights - 1 }
        return endpoints(NotificationCompat.ProgressStyle(), model)
            .setProgress(trip.progressMinutes)
            .setProgressSegments(segments)
            .setProgressPoints(stops.map { NotificationCompat.ProgressStyle.Point(it).setColor(WHITE) })
            .setStyledByProgress(true)
    }

    /** Takeoff and landing icons at the two ends of the bar, and a plane that tilts up, level or down. */
    private fun endpoints(style: NotificationCompat.ProgressStyle, model: FlightLiveUpdate): NotificationCompat.ProgressStyle =
        style
            .setProgressStartIcon(IconCompat.createWithResource(context, R.drawable.ic_takeoff))
            .setProgressEndIcon(IconCompat.createWithResource(context, R.drawable.ic_landing))
            .setProgressTrackerIcon(IconCompat.createWithResource(context, trackerFor(model.phase)))

    private fun trackerFor(phase: FlightLivePhase): Int = when (phase) {
        FlightLivePhase.TAKEOFF -> R.drawable.ic_plane_climb
        FlightLivePhase.DESCENT -> R.drawable.ic_plane_descend
        else -> R.drawable.ic_plane
    }

    /** How far along the map the plane is: this flight's own progress, or the end of the leg at a layover. */
    private fun routeProgress(flight: ActiveFlight, model: FlightLiveUpdate): Float =
        if (flight.status == com.travelblock.app.domain.model.ActiveFlightStatus.Layover) 1f else model.progressFraction

    private fun isPromotionAvailable(): Boolean = Build.VERSION.SDK_INT >= 36 &&
        context.getSystemService(NotificationManager::class.java)?.canPostPromotedNotifications() == true

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun ActiveFlight.liveLegLabel(): String {
        if (!isItinerary) return "Seat ${seat.label}"
        val leg = itineraryLegIndex + if (status == com.travelblock.app.domain.model.ActiveFlightStatus.Layover) 2 else 1
        return "Leg $leg of $itineraryLegCount, seat ${seat.label}"
    }

    companion object {
        const val CHANNEL_ID = "travelblock_live_flight"
        const val NOTIFICATION_ID = 1002
        // The Apron palette: cobalt for flying, a pale blue for breaks.
        private const val ACCENT = 0xFF2745F2.toInt()
        private const val MIST = 0xFFB9C6FF.toInt()
        private const val SIGNAL = 0xFF14C3BA.toInt()
        private const val WHITE = 0xFFFFFFFF.toInt()
    }
}
