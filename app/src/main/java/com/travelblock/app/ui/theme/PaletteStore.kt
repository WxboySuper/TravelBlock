package com.travelblock.app.ui.theme

import android.content.Context

/** Remembers the chosen palette. A plain preference so it is available instantly at startup. */
object PaletteStore {
    private const val FILE = "travelblock_theme"
    // A new key so everyone starts on the redesigned default once, and can pick another from Settings.
    private const val KEY = "palette_v2"

    fun load(context: Context) {
        val id = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null)
        ActivePalette.current = paletteById(id)
    }

    fun select(context: Context, palette: TravelPalette) {
        ActivePalette.current = palette
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, palette.id).apply()
    }
}
