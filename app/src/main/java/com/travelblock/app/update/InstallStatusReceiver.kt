package com.travelblock.app.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.travelblock.app.TravelBlockContainer

class InstallStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        manager?.handleInstallStatus(context, intent)
    }

    companion object { var manager: UpdateManager? = null }
}
