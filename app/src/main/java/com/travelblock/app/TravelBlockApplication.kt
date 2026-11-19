package com.travelblock.app

import android.app.Application
import com.travelblock.app.util.LiveFlightNotificationHelper

class TravelBlockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LiveFlightNotificationHelper(this).createChannel()
    }
}
