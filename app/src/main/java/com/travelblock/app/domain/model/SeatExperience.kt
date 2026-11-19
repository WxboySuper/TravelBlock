package com.travelblock.app.domain.model

/** Where a seat sits across the row: it decides how much of the window you get. */
enum class SeatPosition(val label: String, val windowShare: Float) {
    Window("Window", 1f),
    Middle("Middle", 0.62f),
    Aisle("Aisle", 0.8f),
}

/** What a seat gives you, in words, so a choice in the cabin map means something in the air. */
data class SeatExperience(val position: SeatPosition, val tierName: String, val perks: List<String>)

val SeatOption.position: SeatPosition
    get() = when (label.lastOrNull()) {
        'A', 'F' -> SeatPosition.Window
        'C', 'D' -> SeatPosition.Aisle
        else -> SeatPosition.Middle
    }

val SeatOption.experience: SeatExperience
    get() = SeatExperience(position, ticketClass.tierName, ticketClass.perks + position.perk)

val TicketClass.tierName: String
    get() = when (this) {
        TicketClass.Standard -> "Standard"
        TicketClass.Window -> "Standard"
        TicketClass.ExtraLegroom -> "Extra legroom"
        TicketClass.Premium -> "Premium"
    }

private val TicketClass.perks: List<String>
    get() = when (this) {
        TicketClass.Premium -> listOf("Leather-and-gold cabin lighting", "Largest seat-back map", "Welcome drink on boarding")
        TicketClass.ExtraLegroom -> listOf("Calm green cabin lighting", "Taller seat-back map")
        else -> emptyList()
    }

private val SeatPosition.perk: String
    get() = when (this) {
        SeatPosition.Window -> "Full window: the whole sky, and the wing"
        SeatPosition.Aisle -> "Narrower window, easy to stretch your legs"
        SeatPosition.Middle -> "Smallest window, a view across the row"
    }
