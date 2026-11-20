package com.travelblock.app.ui.screen.book
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import com.travelblock.app.domain.model.experience
import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.data.todoist.TodoistRepository
import com.travelblock.app.ui.components.BoardingPassCard
import com.travelblock.app.ui.components.BoardingPassReveal
import com.travelblock.app.ui.components.BookingFlowStep
import com.travelblock.app.ui.components.BookingFlowTransition
import com.travelblock.app.ui.components.BookingProgressRail
import com.travelblock.app.ui.components.BookingRitualSurface
import com.travelblock.app.ui.components.BookingRitualSurfaceTransition
import com.travelblock.app.ui.components.CompactRouteDocket
import com.travelblock.app.ui.components.DocketFlightDetails
import com.travelblock.app.ui.components.DocketRoute
import com.travelblock.app.ui.components.EmptyState
import com.travelblock.app.ui.components.EmptyStateContent
import com.travelblock.app.ui.components.GateBoardingSequence
import com.travelblock.app.ui.components.GateBoardingInfo
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.components.SoftChip
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.StickyActionScaffold
import com.travelblock.app.ui.components.StickyActionListOptions
import com.travelblock.app.ui.components.TravelDayBackdrop
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.scene.CabinAisleScene
import com.travelblock.app.ui.scene.CheckInDeskScene
import com.travelblock.app.ui.scene.CheckInGateInfo
import com.travelblock.app.ui.scene.JetBridgeScene
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.SkyMist
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.WarmGray
import com.travelblock.app.ui.util.rememberTravelBlockHaptics

/** A real-cabin seat map: a fuselage outline with window markers, row numbers down the aisle, and seat-back shapes. */
@Composable
fun CabinSeatMap(
    seats: List<SeatOption>,
    selectedSeatId: String?,
    seatSelectionMessage: String?,
    pointsBalance: Int,
    onSeatSelected: (String) -> Unit,
) {
    PaperCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Cabin map", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text("Muted seats are already taken.", style = MaterialTheme.typography.bodySmall, color = WarmGray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$pointsBalance pts", style = MaterialTheme.typography.titleLarge, color = DuskBlue)
                    Text(
                        selectedSeatId?.let { "Seat $it" } ?: "Your balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGray,
                    )
                }
            }
            SeatLegend()
            seatSelectionMessage?.let {
                Surface(
                    color = SoftSky,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Sand),
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = DuskBlue,
                    )
                }
            }
            seats.firstOrNull { it.id == selectedSeatId }?.let { SeatPerksCard(it) }
            Fuselage {
                CabinColumnLabels()
                seats.groupBy { it.label.dropLast(1).toInt() }
                    .toSortedMap()
                    .forEach { (rowNumber, rowSeats) ->
                        CabinSeatRow(
                            rowNumber = rowNumber,
                            rowSeats = rowSeats.sortedBy { it.label.last() },
                            selectedSeatId = selectedSeatId,
                            onSeatSelected = onSeatSelected,
                        )
                    }
            }
        }
    }
}

@Composable
private fun Fuselage(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Cream,
        shape = RoundedCornerShape(topStart = 72.dp, topEnd = 72.dp, bottomStart = 26.dp, bottomEnd = 26.dp),
        border = BorderStroke(1.5.dp, Sand),
    ) {
        Column(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 22.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Front of cabin",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = WarmGray,
                textAlign = TextAlign.Center,
            )
            content()
        }
    }
}

@Composable
private fun CabinColumnLabels() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(WindowWidth))
        listOf("A", "B", "C").forEach { ColumnLabel(it, Modifier.weight(1f)) }
        Spacer(Modifier.weight(0.8f))
        listOf("D", "E", "F").forEach { ColumnLabel(it, Modifier.weight(1f)) }
        Spacer(Modifier.width(WindowWidth))
    }
}

@Composable
private fun ColumnLabel(label: String, modifier: Modifier) {
    Text(
        text = label,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = WarmGray,
        textAlign = TextAlign.Center,
    )
}

private val WindowWidth = 6.dp

@Composable
private fun CabinSeatRow(
    rowNumber: Int,
    rowSeats: List<SeatOption>,
    selectedSeatId: String?,
    onSeatSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CabinWindow()
        rowSeats.forEachIndexed { index, seat ->
            if (index == 3) {
                Text(
                    text = rowNumber.toString(),
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    color = Marigold,
                    textAlign = TextAlign.Center,
                )
            }
            SeatButton(
                seat = seat,
                selected = seat.id == selectedSeatId,
                onSelected = { onSeatSelected(seat.id) },
                modifier = Modifier.weight(1f),
            )
        }
        CabinWindow()
    }
}

@Composable
private fun CabinWindow() {
    Box(
        modifier = Modifier
            .width(WindowWidth)
            .height(18.dp)
            .clip(RoundedCornerShape(50))
            .background(SoftSky)
            .border(1.dp, Sand, RoundedCornerShape(50)),
    )
}

@Composable
private fun SeatButton(
    seat: SeatOption,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when {
        selected -> DuskBlue
        !seat.isAvailable -> Sand.copy(alpha = 0.55f)
        else -> seatTint(seat.ticketClass)
    }
    val textColor = when {
        selected -> Paper
        seat.isAvailable -> Ink
        else -> WarmGray.copy(alpha = 0.6f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
        border = BorderStroke(if (selected) 1.dp else seatBorderWidth(seat.ticketClass), if (selected) DuskBlue else seatBorder(seat.ticketClass)),
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .height(seatHeight(seat.ticketClass))
            .semantics {
                contentDescription = if (seat.isAvailable) seat.label else "${seat.label} unavailable"
            }
            .toggleable(
                value = selected,
                enabled = seat.isAvailable || selected,
                role = Role.RadioButton,
                onValueChange = { onSelected() },
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Paper, modifier = Modifier.size(18.dp))
            } else {
                Text(
                    text = seat.label.takeLast(1),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                )
            }
        }
    }
}

/** Better seats are visibly bigger and edged in their tier colour, so a tier reads at a glance. */
private fun seatHeight(ticketClass: TicketClass) = when (ticketClass) {
    TicketClass.Premium -> 52.dp
    TicketClass.ExtraLegroom -> 46.dp
    else -> 40.dp
}

private fun seatBorderWidth(ticketClass: TicketClass) = if (ticketClass == TicketClass.Premium || ticketClass == TicketClass.ExtraLegroom) 1.8.dp else 1.dp

private fun seatBorder(ticketClass: TicketClass): Color = when (ticketClass) {
    TicketClass.Premium -> Color(0xFFC9A24B)
    TicketClass.ExtraLegroom -> Sage
    else -> Sand
}

/** What the highlighted seat gives you in the air: its tier, where it sits across the row, and the perks. */
@Composable
private fun SeatPerksCard(seat: SeatOption) {
    val experience = seat.experience
    Surface(color = seatTint(seat.ticketClass), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, seatBorder(seat.ticketClass))) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Seat ${seat.label} / ${experience.tierName} / ${experience.position.label}", style = MaterialTheme.typography.titleMedium, color = Ink)
            experience.perks.forEach { Text("- $it", style = MaterialTheme.typography.bodySmall, color = Ink) }
        }
    }
}

private fun seatTint(ticketClass: TicketClass): Color = when (ticketClass) {
    TicketClass.Premium -> Color(0xFFF8E3B4)
    TicketClass.ExtraLegroom -> Color(0xFFDCEBDD)
    TicketClass.Window -> Color(0xFFD9E6F2)
    TicketClass.Standard -> SoftSky
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeatLegend() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LegendItem("Standard", SoftSky)
        LegendItem("Window", seatTint(TicketClass.Window))
        LegendItem("Extra legroom", seatTint(TicketClass.ExtraLegroom))
        LegendItem("Premium", seatTint(TicketClass.Premium))
        LegendItem("Taken", Sand.copy(alpha = 0.55f))
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(width = 16.dp, height = 14.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                .background(color)
                .border(1.dp, Sand, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp)),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = WarmGray)
    }
}


