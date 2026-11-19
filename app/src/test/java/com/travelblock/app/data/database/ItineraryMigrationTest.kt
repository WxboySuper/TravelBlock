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

    @Test
    fun `version eight migration adds the per leg task flag`() {
        val sql = mutableListOf<String>()
        val database = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL") sql += args?.first() as String
            null
        } as SupportSQLiteDatabase

        MIGRATION_7_8.migrate(database)

        assertEquals(7, MIGRATION_7_8.startVersion)
        assertEquals(8, MIGRATION_7_8.endVersion)
        assertTrue(sql.single().contains("ADD COLUMN `itineraryTaskPerLeg` INTEGER NOT NULL DEFAULT 0"))
    }

    @Test
    fun `version seven migration adds custom leg and break schedules`() {
        val sql = mutableListOf<String>()
        val database = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL") sql += args?.first() as String
            null
        } as SupportSQLiteDatabase

        MIGRATION_6_7.migrate(database)

        assertEquals(6, MIGRATION_6_7.startVersion)
        assertEquals(7, MIGRATION_6_7.endVersion)
        assertEquals(MIGRATION_6_7_SQL.size, sql.size)
        listOf("itineraryDurationsMinutes", "itineraryBreakMinutes", "itineraryLongBreakMinutes", "itineraryLongBreakEvery")
            .forEach { column -> assertTrue(sql.any { it.contains("ADD COLUMN `$column`") }) }
    }
}
