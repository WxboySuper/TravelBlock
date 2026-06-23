package com.travelblock.app.domain.model

data class FocusDurationOption(
    val minutes: Int,
    val label: String = "${minutes}m",
) {
    init {
        require(minutes > 0) { "Focus duration must be positive." }
    }
}

