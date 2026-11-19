package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.domain.engine.SessionPlanCalculator
import com.travelblock.app.domain.model.SessionBreakPlan
import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.domain.model.TripPreset
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TotalChoices = listOf(60, 120, 180, 240, 360, 480, 600)
private val BlockChoices = listOf(15, 25, 45, 50, 60, 90, 120)
private val BreakChoices = listOf(5, 10, 15, 20, 30)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionPlannerPicker(
    selected: SessionPlan?,
    onSelected: (SessionPlan?) -> Unit,
) {
    var draft by remember { mutableStateOf(PlannerDraft.from(selected)) }
    var customTotal by remember { mutableStateOf("") }
    var customBlock by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(selected != null) }

    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(expanded, { expanded = !expanded; if (expanded) publish(draft, onSelected) else onSelected(null) }, label = { Text("Plan by time") })
        if (!expanded) return@Column
        Kicker("Build your session")
        PlannerChoiceRow("Total focus", TotalChoices, draft.total, { value -> draft = draft.copy(total = value); customTotal = ""; publish(draft, onSelected) })
        OutlinedTextField(
            value = customTotal,
            onValueChange = { value -> customTotal = value.filter(Char::isDigit).take(3); customTotal.toIntOrNull()?.takeIf { it in 1..720 }?.let { draft = draft.copy(total = it); publish(draft, onSelected) } },
            label = { Text("Custom total, minutes (up to 720)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        PlannerChoiceRow("Focus block", BlockChoices, draft.block, { value -> draft = draft.copy(block = value); customBlock = ""; publish(draft, onSelected) })
        OutlinedTextField(
            value = customBlock,
            onValueChange = { value -> customBlock = value.filter(Char::isDigit).take(3); customBlock.toIntOrNull()?.takeIf { it in 10..180 }?.let { draft = draft.copy(block = it); publish(draft, onSelected) } },
            label = { Text("Custom block, minutes (10 to 180)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        PlannerChoiceRow("Break", BreakChoices, draft.breakLength, { value -> draft = draft.copy(breakLength = value); publish(draft, onSelected) })
        FilterChip(draft.longEnabled, { draft = draft.copy(longEnabled = !draft.longEnabled); publish(draft, onSelected) }, label = { Text("Longer break") })
        if (draft.longEnabled) {
            PlannerChoiceRow("Long break", listOf(15, 20, 25, 30), draft.longBreak, { value -> draft = draft.copy(longBreak = value); publish(draft, onSelected) })
            PlannerChoiceRow("Long break every", listOf(3, 4, 5, 6), draft.longEvery, { value -> draft = draft.copy(longEvery = value); publish(draft, onSelected) })
        }
        PlannerPreview(draft)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlannerChoiceRow(label: String, choices: List<Int>, selected: Int, onSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            choices.forEach { value ->
                FilterChip(selected == value, { onSelected(value) }, label = { Text(valueLabel(value)) })
            }
        }
    }
}

@Composable
private fun PlannerPreview(draft: PlannerDraft) {
    val result = SessionPlanCalculator.calculate(draft.toSessionPlan())
    if (result == null) {
        val count = (draft.total + draft.block - 1) / draft.block
        Text("$count legs exceeds the ${TripPreset.MAX_TRIP_LEGS}-leg limit. Increase the block length.", color = MaterialTheme.colorScheme.error)
        return
    }
    val end = LocalTime.now().plusMinutes((result.totalFocusMinutes + result.totalBreakMinutes).toLong())
    val clock = end.format(DateTimeFormatter.ofPattern("HH:mm"))
    Text("${result.legCount} legs | ${result.totalFocusMinutes} min focus | ${result.totalBreakMinutes} min breaks | ends about $clock")
    Text("Focus is split evenly across legs.", style = MaterialTheme.typography.bodySmall)
}

private fun publish(draft: PlannerDraft, onSelected: (SessionPlan?) -> Unit) {
    val plan = draft.toSessionPlan()
    onSelected(plan.takeIf { SessionPlanCalculator.calculate(it) != null })
}

private fun valueLabel(value: Int): String = if (value % 60 == 0) "${value / 60}h" else "${value}m"

private data class PlannerDraft(
    val total: Int = 240,
    val block: Int = 50,
    val breakLength: Int = 10,
    val longBreak: Int = 20,
    val longEvery: Int = 4,
    val longEnabled: Boolean = true,
) {
    fun toSessionPlan() = SessionPlan(
        totalFocusMinutes = total,
        blockMinutes = block,
        breaks = SessionBreakPlan(breakLength, longBreak.takeIf { longEnabled }, longEvery.takeIf { longEnabled }),
    )

    companion object {
        fun from(plan: SessionPlan?) = plan?.let {
            PlannerDraft(
                total = it.totalFocusMinutes,
                block = it.blockMinutes,
                breakLength = it.breaks.regularMinutes,
                longBreak = it.breaks.longMinutes ?: 20,
                longEvery = it.breaks.longEveryBlocks ?: 4,
                longEnabled = it.breaks.longMinutes != null,
            )
        } ?: PlannerDraft()
    }
}
