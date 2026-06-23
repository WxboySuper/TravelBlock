package com.travelblock.app.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `unlocked_cosmetics` (
                `id` TEXT NOT NULL,
                `cosmeticType` TEXT NOT NULL,
                `cosmeticId` TEXT NOT NULL,
                `unlockedAt` INTEGER NOT NULL,
                `pricePaid` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        if (!db.hasColumn("flight_logs", "focusObjective")) {
            db.execSQL(
                """
                ALTER TABLE `flight_logs`
                ADD COLUMN `focusObjective` TEXT NOT NULL DEFAULT ''
                """.trimIndent(),
            )
        }

        if (!db.hasColumn("flight_logs", "focusTag")) {
            db.execSQL(
                """
                ALTER TABLE `flight_logs`
                ADD COLUMN `focusTag` TEXT NOT NULL DEFAULT ''
                """.trimIndent(),
            )
        }
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `active_flight_session` (
                `sessionId` INTEGER NOT NULL,
                `originCode` TEXT NOT NULL,
                `originIcao` TEXT NOT NULL,
                `originIata` TEXT,
                `originName` TEXT NOT NULL,
                `originCity` TEXT NOT NULL,
                `originStateOrRegion` TEXT,
                `originCountry` TEXT NOT NULL,
                `originLatitude` REAL NOT NULL,
                `originLongitude` REAL NOT NULL,
                `originElevationFeet` INTEGER,
                `destinationCode` TEXT NOT NULL,
                `destinationIcao` TEXT NOT NULL,
                `destinationIata` TEXT,
                `destinationName` TEXT NOT NULL,
                `destinationCity` TEXT NOT NULL,
                `destinationStateOrRegion` TEXT,
                `destinationCountry` TEXT NOT NULL,
                `destinationLatitude` REAL NOT NULL,
                `destinationLongitude` REAL NOT NULL,
                `destinationElevationFeet` INTEGER,
                `durationMinutes` INTEGER NOT NULL,
                `distanceMiles` REAL NOT NULL,
                `seatId` TEXT NOT NULL,
                `seatLabel` TEXT NOT NULL,
                `seatTicketClass` TEXT NOT NULL,
                `seatPointsCost` INTEGER NOT NULL,
                `seatDescription` TEXT NOT NULL,
                `seatIsAvailable` INTEGER NOT NULL,
                `flightNumber` TEXT NOT NULL,
                `startedAt` INTEGER NOT NULL,
                `plannedArrivalAt` INTEGER NOT NULL,
                `focusObjective` TEXT NOT NULL,
                `focusTag` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                PRIMARY KEY(`sessionId`)
            )
            """.trimIndent(),
        )
    }
}

private fun SupportSQLiteDatabase.hasColumn(
    tableName: String,
    columnName: String,
): Boolean {
    query("PRAGMA table_info(`$tableName`)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (nameIndex >= 0 && cursor.getString(nameIndex) == columnName) {
                return true
            }
        }
    }
    return false
}
