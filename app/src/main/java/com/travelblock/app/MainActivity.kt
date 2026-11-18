package com.travelblock.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.travelblock.app.ui.TravelBlockApp
import com.travelblock.app.ui.theme.TravelBlockTheme
import com.travelblock.app.util.NotificationHelper

class MainActivity : ComponentActivity() {
    private var openArrivalRequested by mutableStateOf(false)
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openArrivalRequested = intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_ARRIVAL, false)
        enableEdgeToEdge()
        setContent {
            TravelBlockTheme {
                TravelBlockApp(openArrivalRequested = openArrivalRequested)
            }
        }
    }

    fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openArrivalRequested = intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_ARRIVAL, false)
    }
}
