package com.travelblock.app.ui.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

/** The announcer's voice as offered to the traveller. */
data class VoiceOption(val name: String, val label: String, val quality: Int, val needsNetwork: Boolean, val installed: Boolean)

/** Remembers which announcer voice was picked. Empty means "choose the most natural one automatically". */
object AnnouncerVoiceStore {
    private const val FILE = "travelblock_voice"
    private const val KEY = "voice_name"

    fun saved(context: Context): String? =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null)?.takeIf { it.isNotBlank() }

    fun save(context: Context, name: String?) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, name.orEmpty()).apply()
    }
}

object AnnouncerVoices {
    /** English voices, most natural first: high quality, installed on the device, then ones that work offline. */
    fun english(engine: TextToSpeech): List<VoiceOption> {
        val voices = runCatching { engine.voices }.getOrNull().orEmpty().filter { it.locale.language == "en" }
        return voices.sortedWith(naturalness()).mapIndexed { index, voice -> voice.toOption(index) }
    }

    /** The voice to use: the saved one if it still exists, otherwise the most natural English voice. */
    fun pick(engine: TextToSpeech, savedName: String?): Voice? {
        val voices = runCatching { engine.voices }.getOrNull().orEmpty().filter { it.locale.language == "en" }
        return voices.firstOrNull { it.name == savedName } ?: voices.sortedWith(naturalness()).firstOrNull { !it.isMissing() }
    }

    private fun naturalness(): Comparator<Voice> = compareBy<Voice>(
        { it.isMissing() },
        { -it.quality },
        { it.isNetworkConnectionRequired },
        { it.locale.country != "US" },
        { it.name },
    )

    private fun Voice.isMissing() = features?.contains("notInstalled") == true

    private fun Voice.toOption(index: Int): VoiceOption {
        val region = locale.getDisplayCountry(Locale.ENGLISH).ifBlank { locale.displayLanguage }
        val tier = when {
            quality >= Voice.QUALITY_VERY_HIGH -> "Natural"
            quality >= Voice.QUALITY_HIGH -> "High quality"
            else -> "Standard"
        }
        val extras = buildList {
            if (isNetworkConnectionRequired) add("needs internet")
            if (isMissing()) add("not downloaded")
        }
        val suffix = if (extras.isEmpty()) "" else " (${extras.joinToString(", ")})"
        return VoiceOption(name, "Voice ${index + 1} - $region, $tier$suffix", quality, isNetworkConnectionRequired, !isMissing())
    }
}
