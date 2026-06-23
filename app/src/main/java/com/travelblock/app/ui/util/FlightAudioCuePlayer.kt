package com.travelblock.app.ui.util

import android.media.AudioManager
import android.media.ToneGenerator

enum class FlightAudioCue {
    GateCleared,
    FlightStarted,
    FlightArrived,
    FlightDiverted,
}

class FlightAudioCuePlayer {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)

    fun play(cue: FlightAudioCue, enabled: Boolean) {
        if (!enabled) return
        when (cue) {
            FlightAudioCue.GateCleared -> toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 140)
            FlightAudioCue.FlightStarted -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            FlightAudioCue.FlightArrived -> toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 260)
            FlightAudioCue.FlightDiverted -> toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 220)
        }
    }

    fun release() {
        toneGenerator.release()
    }
}
