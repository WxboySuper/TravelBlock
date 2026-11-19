package com.travelblock.app.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale

enum class FlightAudioCue { GateCleared, FlightStarted, FlightArrived, FlightDiverted, CheckIn, PrepareForLanding }

@JvmInline
value class FrequencyHertz(val value: Double)

@JvmInline
value class DurationSeconds(val value: Double)

@JvmInline
value class SampleRateHertz(val value: Int)

@JvmInline
value class AnnouncementText(val value: String)

data class AudioPlaybackSettings(val soundEnabled: Boolean, val announcementsEnabled: Boolean = true)

private data class Chime(val frequencies: List<FrequencyHertz>, val duration: DurationSeconds)
private data class SampleRange(val index: Int, val count: Int)

object FlightChimeGenerator {
    const val SampleRate = 22_050
    private val sampleRate = SampleRateHertz(SampleRate)

    fun generate(cue: FlightAudioCue): ShortArray = when (cue) {
        FlightAudioCue.GateCleared -> melody(chime(DurationSeconds(0.48), listOf(FrequencyHertz(660.0), FrequencyHertz(880.0))))
        FlightAudioCue.FlightStarted -> melody(chime(DurationSeconds(0.72), listOf(FrequencyHertz(659.25), FrequencyHertz(523.25))))
        FlightAudioCue.FlightArrived -> melody(chime(DurationSeconds(0.9), listOf(FrequencyHertz(784.0), FrequencyHertz(659.25), FrequencyHertz(523.25))))
        FlightAudioCue.FlightDiverted -> tone(chime(DurationSeconds(0.3), listOf(FrequencyHertz(300.0))))
        FlightAudioCue.CheckIn -> tone(chime(DurationSeconds(0.16), listOf(FrequencyHertz(1046.5))))
        FlightAudioCue.PrepareForLanding -> tone(chime(DurationSeconds(0.12), listOf(FrequencyHertz(440.0))))
    }

    private fun chime(duration: DurationSeconds, frequencies: List<FrequencyHertz>) = Chime(frequencies, duration)

    private fun tone(chime: Chime): ShortArray = mixed(chime)

    private fun mixed(chime: Chime): ShortArray {
        val count = (sampleRate.value * chime.duration.value).toInt()
        return ShortArray(count) { index ->
            val time = index.toDouble() / sampleRate.value
            val envelope = envelope(SampleRange(index, count))
            val wave = chime.frequencies.sumOf { kotlin.math.sin(2.0 * Math.PI * it.value * time) } / chime.frequencies.size
            (wave * envelope * Short.MAX_VALUE * 0.58).toInt().toShort()
        }
    }

    private fun melody(chime: Chime): ShortArray {
        val count = (sampleRate.value * chime.duration.value).toInt()
        val noteLength = count.toDouble() / chime.frequencies.size
        return ShortArray(count) { index ->
            val note = (index / noteLength).toInt().coerceAtMost(chime.frequencies.lastIndex)
            val noteSample = (index - note * noteLength).toInt()
            val noteSamples = noteLength.toInt()
            val toneSample = kotlin.math.sin(2.0 * Math.PI * chime.frequencies[note].value * noteSample / sampleRate.value)
            (toneSample * envelope(SampleRange(index, count)) * envelope(SampleRange(noteSample, noteSamples)) * Short.MAX_VALUE * 0.72).toInt().toShort()
        }
    }

    private fun envelope(range: SampleRange): Double {
        val fade = (sampleRate.value * 0.025).toInt().coerceAtLeast(1)
        return minOf(1.0, range.index.toDouble() / fade, (range.count - range.index - 1).toDouble() / fade).coerceAtLeast(0.0)
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
    private var pendingSpeech: AnnouncementText? = null

    fun play(cue: FlightAudioCue, settings: AudioPlaybackSettings) {
        if (!settings.soundEnabled) return
        playRawOrGenerated(cue)
        announce(cue, settings)
    }

    fun announce(cue: FlightAudioCue, settings: AudioPlaybackSettings) {
        if (!settings.soundEnabled || !settings.announcementsEnabled) return
        val announcement = FlightAnnouncements.text(cue)
        if (announcement != null) speak(AnnouncementText(announcement))
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

    private fun speak(text: AnnouncementText) {
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

    private fun speakReady(text: AnnouncementText) {
        textToSpeech?.speak(text.value, TextToSpeech.QUEUE_ADD, null, text.value.hashCode().toString())
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
