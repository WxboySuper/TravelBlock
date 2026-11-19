package com.travelblock.app.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lowers the music while the PA speaks, the way a navigation app does: the announcement takes transient audio
 * focus (other apps may duck), and gives it back when the last queued announcement is done.
 */
class AnnouncementDucking(context: Context) {
    private val audioManager = context.applicationContext.getSystemService(AudioManager::class.java)
    private val queued = AtomicInteger(0)
    private var request: AudioFocusRequest? = null

    /** Call before an announcement is queued. */
    @Synchronized
    fun begin() {
        queued.incrementAndGet()
        if (request != null) return
        val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(ATTRIBUTES)
            .setOnAudioFocusChangeListener { }
            .build()
        audioManager?.requestAudioFocus(focus)
        request = focus
    }

    /** Call when an announcement finished, failed, or never started. */
    @Synchronized
    fun end() {
        if (queued.updateAndGet { (it - 1).coerceAtLeast(0) } > 0) return
        abandon()
    }

    @Synchronized
    fun abandon() {
        queued.set(0)
        request?.let { audioManager?.abandonAudioFocusRequest(it) }
        request = null
    }

    companion object {
        /** Spoken guidance: the same usage maps apps use, so music ducks instead of pausing. */
        val ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    }
}
