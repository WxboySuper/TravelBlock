package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.StoreItem
import com.travelblock.app.domain.model.StoreItemCategory

object StoreCatalog {
    val items: List<StoreItem> = listOf(
        StoreItem(
            id = "boarding_pass_skyline",
            name = "Skyline Boarding Pass",
            description = "A crisp blue boarding pass treatment for completed focus flights.",
            category = StoreItemCategory.BoardingPassTheme,
            cost = 120,
        ),
        StoreItem(
            id = "boarding_pass_sunset",
            name = "Sunset Boarding Pass",
            description = "Warm amber accents for a softer evening travel feel.",
            category = StoreItemCategory.BoardingPassTheme,
            cost = 160,
        ),
        StoreItem(
            id = "aircraft_regional_jet",
            name = "Regional Jet",
            description = "A compact aircraft marker for short focus hops.",
            category = StoreItemCategory.AircraftIcon,
            cost = 100,
        ),
        StoreItem(
            id = "aircraft_twin_turboprop",
            name = "Twin Turboprop",
            description = "A grounded, regional aircraft icon for calm study routes.",
            category = StoreItemCategory.AircraftIcon,
            cost = 90,
        ),
        StoreItem(
            id = "cabin_quiet_gate",
            name = "Quiet Gate Ambience",
            description = "A placeholder cabin ambience pack for future audio controls.",
            category = StoreItemCategory.CabinAmbience,
            cost = 80,
        ),
    )
}
