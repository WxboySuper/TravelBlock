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

internal val MIGRATION_4_5_SQL = listOf(
    "ALTER TABLE `active_flight_session` ADD COLUMN `todoistTaskId` TEXT",
    "ALTER TABLE `active_flight_session` ADD COLUMN `todoistTaskContent` TEXT",
    "ALTER TABLE `flight_logs` ADD COLUMN `todoistTaskId` TEXT",
    "ALTER TABLE `flight_logs` ADD COLUMN `todoistTaskContent` TEXT",
)

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        MIGRATION_4_5_SQL.forEach(db::execSQL)
    }
}

internal val MIGRATION_5_6_SQL = listOf(
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryCodes` TEXT",
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryLabel` TEXT",
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryLegIndex` INTEGER NOT NULL DEFAULT 0",
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryLegCount` INTEGER NOT NULL DEFAULT 1",
    "ALTER TABLE `active_flight_session` ADD COLUMN `layoverEndsAtEpochMillis` INTEGER",
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryPointsEarned` INTEGER NOT NULL DEFAULT 0",
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryDistanceMiles` REAL NOT NULL DEFAULT 0.0",
)

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        MIGRATION_5_6_SQL.forEach(db::execSQL)
    }
}

internal val MIGRATION_6_7_SQL = listOf(
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryDurationsMinutes` TEXT",
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryBreakMinutes` TEXT",
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryLongBreakMinutes` INTEGER",
    "ALTER TABLE `active_flight_session` ADD COLUMN `itineraryLongBreakEvery` INTEGER",
)

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        MIGRATION_6_7_SQL.forEach(db::execSQL)
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
