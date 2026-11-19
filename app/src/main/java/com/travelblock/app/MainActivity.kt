package com.travelblock.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.update.UpdateManager
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.travelblock.app.ui.TravelBlockApp
import com.travelblock.app.ui.theme.TravelBlockTheme
import com.travelblock.app.util.NotificationHelper

class MainActivity : ComponentActivity() {
    private val updates by lazy { TravelBlockContainer(applicationContext).updateManager }
    private var firstResume = true
    private var openArrivalRequested by mutableStateOf(false)
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openArrivalRequested = intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_ARRIVAL, false)
        lifecycleScope.launch { updates.check() }
        // The app is always the daylight journal, so system bar icons stay dark by default; screens with dark
        // scenery (Home, onboarding scenes, cockpit) switch to light icons themselves.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
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

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (firstResume) firstResume = false else updates.checkOnForeground()
            if (Build.VERSION.SDK_INT < 26 || packageManager.canRequestPackageInstalls()) updates.resumePendingInstall()
        }
    }
}
