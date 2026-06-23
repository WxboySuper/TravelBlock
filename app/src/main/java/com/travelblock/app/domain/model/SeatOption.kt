package com.travelblock.app.domain.model

data class SeatOption(
    val id: String,
    val label: String,
    val ticketClass: TicketClass,
    val pointsCost: Int,
    val description: String,
    val isAvailable: Boolean = true,
)
