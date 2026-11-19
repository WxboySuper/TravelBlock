package com.travelblock.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray

/** Shared building blocks for the warm "travel journal" look. */

@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val base = modifier
    Surface(
        modifier = if (onClick != null) base.clip(shape).clickable(onClick = onClick) else base,
        shape = shape,
        color = if (selected) SoftSky else Paper,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) DuskBlue.copy(alpha = 0.55f) else Sand),
        shadowElevation = 0.dp,
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

/** Small round-cornered label, like a luggage tag or a passport stamp. */
@Composable
fun StampPill(text: String, modifier: Modifier = Modifier, color: Color = Marigold, textColor: Color = Ink) {
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = color.copy(alpha = 0.22f), border = BorderStroke(1.dp, color.copy(alpha = 0.6f))) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
fun SoftChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) DuskBlue else Paper,
        border = BorderStroke(1.dp, if (selected) DuskBlue else Sand),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Paper else Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Surface(
        modifier = modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp)).clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = if (enabled) Terracotta else Sand,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium, color = if (enabled) Paper else WarmGray)
        }
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, Sand),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleSmall, color = DuskBlue)
        }
    }
}

/** A short quiet line above a heading, set in the condensed face and left in sentence case. */
@Composable
fun Kicker(text: String, modifier: Modifier = Modifier, color: Color = WarmGray) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = color, modifier = modifier)
}

@Composable
fun SheetHandle(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.height(5.dp).width(44.dp).background(Sand, RoundedCornerShape(50)))
    }
}


@Composable
fun MetricColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Kicker(label)
        Text(value, style = MaterialTheme.typography.titleLarge, color = Ink)
    }
}

@Composable
fun MetricRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) { content() }
}

val JournalBackground: Color get() = Cream
