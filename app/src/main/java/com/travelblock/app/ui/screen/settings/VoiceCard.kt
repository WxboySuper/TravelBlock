package com.travelblock.app.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray
import com.travelblock.app.ui.util.AnnouncerVoiceStore
import com.travelblock.app.ui.util.AnnouncerVoices
import com.travelblock.app.ui.util.VoiceOption

private const val PreviewLine = "Ladies and gentlemen, welcome aboard. Please fasten your seat belts, and enjoy the flight."

/** Settings card for choosing and previewing the announcer's voice. */
@Composable
fun VoiceCard() {
    val context = LocalContext.current
    var voices by remember { mutableStateOf<List<VoiceOption>>(emptyList()) }
    var chosen by remember { mutableStateOf(AnnouncerVoiceStore.saved(context)) }
    val engine = rememberSpeechEngine(context) { voices = AnnouncerVoices.english(it) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Announcer voice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Automatic picks the most natural English voice installed on this phone. Tap a voice to hear it.",
                style = MaterialTheme.typography.bodySmall, color = WarmGray,
            )
            VoiceRow("Automatic (most natural)", selected = chosen == null) {
                chosen = null
                AnnouncerVoiceStore.save(context, null)
                preview(engine(), null)
            }
            voices.take(12).forEach { option ->
                VoiceRow(option.label, selected = chosen == option.name, enabled = option.installed) {
                    chosen = option.name
                    AnnouncerVoiceStore.save(context, option.name)
                    preview(engine(), option.name)
                }
            }
            OutlinedButton(onClick = { openVoiceSettings(context) }, shape = RoundedCornerShape(12.dp)) {
                Text("Get more voices")
            }
        }
    }
}

@Composable
private fun rememberSpeechEngine(context: Context, onReady: (TextToSpeech) -> Unit): () -> TextToSpeech? {
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var created: TextToSpeech? = null
        created = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine = created
                created?.let(onReady)
            }
        }
        onDispose {
            created?.shutdown()
            engine = null
        }
    }
    return { engine }
}

private fun preview(engine: TextToSpeech?, voiceName: String?) {
    val tts = engine ?: return
    val voice = AnnouncerVoices.pick(tts, voiceName)
    if (voice != null) tts.voice = voice
    tts.setSpeechRate(0.95f)
    tts.setPitch(1.0f)
    tts.speak(PreviewLine, TextToSpeech.QUEUE_FLUSH, null, "voice-preview")
}

private fun openVoiceSettings(context: Context) {
    val intents = listOf(Intent("com.android.settings.TTS_SETTINGS"), Intent("android.settings.SETTINGS"))
    val target = intents.firstOrNull { it.resolveActivity(context.packageManager) != null } ?: return
    context.startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

@Composable
private fun VoiceRow(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val border = if (selected) BorderStroke(2.dp, Terracotta) else BorderStroke(1.dp, Sand)
    Surface(
        onClick = onClick, enabled = enabled, shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background, border = border, modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label + if (selected) "  (in use)" else "",
                color = if (enabled) Ink else WarmGray,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}
