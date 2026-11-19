package com.travelblock.app.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

data class AnnouncementDetails(val destination: String, val localTime: String? = null)

object FlightAnnouncements {
    /** Spoken text for a cue. Nothing about a specific flight is ever invented: without details the lines stay generic. */
    fun text(cue: FlightAudioCue, details: AnnouncementDetails? = null): String? = when (cue) {
        FlightAudioCue.GateCleared -> details?.let { "Now boarding your flight to ${it.destination}" } ?: "Now boarding"
        FlightAudioCue.FlightStarted -> "Cabin crew, prepare for takeoff"
        FlightAudioCue.FlightArrived -> arrivedText(details)
        FlightAudioCue.PrepareForLanding -> "Cabin crew, prepare for landing"
        else -> null
    }

    private fun arrivedText(details: AnnouncementDetails?): String {
        if (details == null) return "Welcome. We have arrived."
        val time = details.localTime?.let { " Local time is $it." }.orEmpty()
        return "Welcome to ${details.destination}.$time"
    }
}

class FlightAudioCuePlayer(context: Context) {
    private val appContext = context.applicationContext
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: AnnouncementText? = null
    private val ducking = AnnouncementDucking(appContext)

    fun play(cue: FlightAudioCue, settings: AudioPlaybackSettings, details: AnnouncementDetails? = null) {
        if (!settings.soundEnabled) return
        playRawOrGenerated(cue)
        announce(cue, settings, details)
    }

    fun announce(cue: FlightAudioCue, settings: AudioPlaybackSettings, details: AnnouncementDetails? = null) {
        if (!settings.soundEnabled || !settings.announcementsEnabled) return
        val announcement = FlightAnnouncements.text(cue, details)
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
        textToSpeech?.let { engine ->
            val wanted = AnnouncerVoices.pick(engine, AnnouncerVoiceStore.saved(appContext))
            if (wanted != null && engine.voice?.name != wanted.name) engine.voice = wanted
        }
        ducking.begin()
        val result = textToSpeech?.speak(text.value, TextToSpeech.QUEUE_ADD, null, text.value.hashCode().toString())
        if (result != TextToSpeech.SUCCESS) ducking.end()
    }

    private fun configureSpeech() {
        val engine = textToSpeech ?: return
        engine.language = Locale.US
        engine.setAudioAttributes(AnnouncementDucking.ATTRIBUTES)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = ducking.end()
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = ducking.end()
            override fun onStop(utteranceId: String?, interrupted: Boolean) = ducking.end()
        })
        // Natural speech: the engine's own pace and pitch, with the most natural installed voice (or the one picked in Settings).
        engine.setSpeechRate(0.95f)
        engine.setPitch(1.0f)
        AnnouncerVoices.pick(engine, AnnouncerVoiceStore.saved(appContext))?.let { engine.voice = it }
    }

    fun release() {
        textToSpeech?.shutdown()
        ducking.abandon()
        textToSpeech = null
        ttsReady = false
        pendingSpeech = null
    }
}

/** What the PA says about an airport: its city and, on arrival, the local (solar) time there. */
fun com.travelblock.app.domain.model.Airport.announcement(withTime: Boolean): AnnouncementDetails {
    val city = city.ifBlank { displayCode }
    if (!withTime) return AnnouncementDetails(city)
    val hour = com.travelblock.app.ui.scene.localSolarHour(java.time.Instant.now(), longitude)
    val minutes = (hour * 60).toInt() % (24 * 60)
    val time = java.time.LocalTime.of(minutes / 60, minutes % 60)
    return AnnouncementDetails(city, time.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)))
}
