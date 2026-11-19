package com.travelblock.app.data.todoist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TodoistParserTest {
    @Test fun parsesUnknownFieldsNullDueAndCursor() {
        val page = TodoistParser.parseTasks(
            """{"results":[{"id":"6abc","content":"Write notes","description":"","project_id":"p1","due":null,"priority":3,"labels":[],"checked":false,"completed_at":null,"new_api_field":true}],"next_cursor":"cursor-next","extra":"ignored"}""",
            mapOf("p1" to "Work"),
        )
        assertEquals("cursor-next", page.nextCursor)
        assertEquals("6abc", page.tasks.single().id)
        assertEquals("Work", page.tasks.single().projectName)
        assertNull(page.tasks.single().dueLabel)
        assertEquals(3, page.tasks.single().priority)
    }

    @Test fun parsesDueStringAndProjectList() {
        val projects = TodoistParser.parseProjects("""{"results":[{"id":"p","name":"Home","unknown":1}]}""")
        val page = TodoistParser.parseTasks("""{"results":[{"id":"t","content":"Clean","project_id":"p","due":{"date":"2026-11-18","string":"today"},"priority":1}],"next_cursor":null}""", projects)
        assertEquals("Home", page.tasks.single().projectName)
        assertEquals("today", page.tasks.single().dueLabel)
        assertNull(page.nextCursor)
    }
}
