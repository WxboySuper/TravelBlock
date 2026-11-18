package com.travelblock.app.ui.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Shows light status-bar icons while the screen is up (for scenery behind the bar), restoring the previous style on leave. */
@Composable
fun LightStatusBarIcons(light: Boolean = true) {
    val view = LocalView.current
    DisposableEffect(view, light) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = !light
        onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
    }
}
