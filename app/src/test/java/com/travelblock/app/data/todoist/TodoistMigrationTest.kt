package com.travelblock.app.data.todoist

import com.travelblock.app.data.database.MIGRATION_4_5
import com.travelblock.app.data.database.MIGRATION_4_5_SQL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoistMigrationTest {
    @Test fun migrationAddsTaskLinkColumnsAndAdvancesDatabaseVersion() {
        assertEquals(4, MIGRATION_4_5.startVersion)
        assertEquals(5, MIGRATION_4_5.endVersion)
        assertEquals(4, MIGRATION_4_5_SQL.size)
        listOf("active_flight_session", "flight_logs").forEach { table ->
            assertTrue(MIGRATION_4_5_SQL.any { it.contains("`$table` ADD COLUMN `todoistTaskId`") })
            assertTrue(MIGRATION_4_5_SQL.any { it.contains("`$table` ADD COLUMN `todoistTaskContent`") })
        }
    }
}
