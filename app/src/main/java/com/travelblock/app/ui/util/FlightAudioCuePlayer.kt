package com.travelblock.app.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale

enum class FlightAudioCue { GateCleared, FlightStarted, FlightArrived, FlightDiverted, CheckIn, PrepareForLanding }

object FlightChimeGenerator {
    const val SampleRate = 22_050

    fun generate(cue: FlightAudioCue): ShortArray = when (cue) {
        FlightAudioCue.GateCleared -> melody(arrayOf(660.0, 880.0), 0.48)
        FlightAudioCue.FlightStarted -> melody(arrayOf(659.25, 523.25), 0.72)
        FlightAudioCue.FlightArrived -> melody(arrayOf(784.0, 659.25, 523.25), 0.9)
        FlightAudioCue.FlightDiverted -> tone(arrayOf(300.0), 0.3)
        FlightAudioCue.CheckIn -> tone(arrayOf(1046.5), 0.16)
        FlightAudioCue.PrepareForLanding -> tone(arrayOf(440.0), 0.12)
    }

    private fun tone(frequencies: Array<Double>, seconds: Double): ShortArray = mixed(frequencies, seconds)

    private fun mixed(frequencies: Array<Double>, seconds: Double): ShortArray {
        val count = (SampleRate * seconds).toInt()
        return ShortArray(count) { index ->
            val time = index.toDouble() / SampleRate
            val envelope = envelope(index, count)
            val wave = frequencies.sumOf { kotlin.math.sin(2.0 * Math.PI * it * time) } / frequencies.size
            (wave * envelope * Short.MAX_VALUE * 0.58).toInt().toShort()
        }
    }

    private fun melody(frequencies: Array<Double>, seconds: Double): ShortArray {
        val count = (SampleRate * seconds).toInt()
        val noteLength = count.toDouble() / frequencies.size
        return ShortArray(count) { index ->
            val note = (index / noteLength).toInt().coerceAtMost(frequencies.lastIndex)
            val noteSample = (index - note * noteLength).toInt()
            val noteSamples = noteLength.toInt()
            val toneSample = kotlin.math.sin(2.0 * Math.PI * frequencies[note] * noteSample / SampleRate)
            (toneSample * envelope(index, count) * envelope(noteSample, noteSamples) * Short.MAX_VALUE * 0.72).toInt().toShort()
        }
    }

    private fun envelope(index: Int, count: Int): Double {
        val fade = (SampleRate * 0.025).toInt().coerceAtLeast(1)
        return minOf(1.0, index.toDouble() / fade, (count - index - 1).toDouble() / fade).coerceAtLeast(0.0)
    }
}

data class AnnouncementDetails(val destination: String, val localTime: String)

object FlightAnnouncements {
    fun text(cue: FlightAudioCue, details: AnnouncementDetails = AnnouncementDetails("Tulsa", "4:53 PM")): String? = when (cue) {
        FlightAudioCue.GateCleared -> "Now boarding flight TB443 to Tulsa at gate L4"
        FlightAudioCue.FlightStarted -> "Cabin crew, prepare for takeoff"
        FlightAudioCue.FlightArrived -> "Welcome to ${details.destination}. Local time is ${details.localTime}."
        FlightAudioCue.PrepareForLanding -> "Cabin crew, prepare for landing"
        else -> null
    }
}

class FlightAudioCuePlayer(context: Context) {
    private val appContext = context.applicationContext
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: String? = null

    fun play(cue: FlightAudioCue, enabled: Boolean, announcementsEnabled: Boolean = true) {
        if (!enabled) return
        playRawOrGenerated(cue)
        announce(cue, enabled, announcementsEnabled)
    }

    fun announce(cue: FlightAudioCue, enabled: Boolean, announcementsEnabled: Boolean = true) {
        if (!enabled || !announcementsEnabled) return
        val announcement = FlightAnnouncements.text(cue)
        if (announcement != null) speak(announcement)
    }

    private fun playRawOrGenerated(cue: FlightAudioCue) {
        val resourceName = cue.resourceName() ?: return
        val resourceId = appContext.resources.getIdentifier(resourceName, "raw", appContext.packageName)
        if (resourceId != 0) {
            MediaPlayer.create(appContext, resourceId)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
            return
        }
        val samples = FlightChimeGenerator.generate(cue)
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(FlightChimeGenerator.SampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        track.setNotificationMarkerPosition(samples.size)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack) = track.release()
            override fun onPeriodicNotification(track: AudioTrack) = Unit
        })
        track.play()
    }

    private fun FlightAudioCue.resourceName(): String? = when (this) {
        FlightAudioCue.GateCleared -> "cue_gate_cleared"
        FlightAudioCue.FlightStarted -> "cue_flight_started"
        FlightAudioCue.FlightArrived -> "cue_flight_arrived"
        FlightAudioCue.FlightDiverted -> "cue_flight_diverted"
        FlightAudioCue.CheckIn -> "cue_check_in"
        FlightAudioCue.PrepareForLanding -> null
    }

    private fun speak(text: String) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(appContext) { status ->
                ttsReady = status == TextToSpeech.SUCCESS
                if (ttsReady) {
                    configureSpeech()
                    pendingSpeech?.let { speakReady(it) }
                    pendingSpeech = null
                }
            }
        }
        if (ttsReady) speakReady(text) else pendingSpeech = text
    }

    private fun speakReady(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_ADD, null, text.hashCode().toString())
    }

    private fun configureSpeech() {
        textToSpeech?.language = Locale.US
        textToSpeech?.setSpeechRate(0.86f)
        textToSpeech?.setPitch(1.12f)
    }

    fun release() {
        textToSpeech?.shutdown()
        textToSpeech = null
        ttsReady = false
        pendingSpeech = null
    }
}
