package com.travelblock.app.domain.model

data class StoreItem(
    val id: String,
    val name: String,
    val description: String,
    val category: StoreItemCategory,
    val cost: Int,
)

enum class StoreItemCategory {
    BoardingPassTheme,
    AircraftIcon,
    CabinAmbience,
}
