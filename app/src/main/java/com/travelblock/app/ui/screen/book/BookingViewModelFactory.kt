package com.travelblock.app.ui.screen.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.travelblock.app.domain.model.DraftFlightBooking
import java.time.Clock

class BookingViewModelFactory(
    private val draftFlightBooking: DraftFlightBooking?,
    private val pointsBalance: Int = 0,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookingViewModel(draftFlightBooking, pointsBalance, clock) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
