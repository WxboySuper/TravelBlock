package com.travelblock.app.data.database

import androidx.sqlite.db.SupportSQLiteDatabase
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItineraryMigrationTest {
    @Test
    fun `version six migration adds durable itinerary and layover fields`() {
        val sql = mutableListOf<String>()
        val database = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL") sql += args?.first() as String
            null
        } as SupportSQLiteDatabase

        MIGRATION_5_6.migrate(database)

        assertEquals(5, MIGRATION_5_6.startVersion)
        assertEquals(6, MIGRATION_5_6.endVersion)
        assertEquals(MIGRATION_5_6_SQL.size, sql.size)
        listOf("itineraryCodes", "itineraryLabel", "itineraryLegIndex", "itineraryLegCount", "layoverEndsAtEpochMillis", "itineraryPointsEarned", "itineraryDistanceMiles")
            .forEach { column -> assertTrue(sql.any { it.contains("ADD COLUMN `$column`") }) }
        assertTrue(sql.any { it.contains("`itineraryLegCount` INTEGER NOT NULL DEFAULT 1") })
    }
}
