package com.travelblock.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskRefsTest {
    private val tasks = listOf(TaskRef("1", "Write chapter, part one"), TaskRef("2", "Review notes"))

    @Test
    fun `several tasks survive being stored in the single id and content fields`() {
        val decoded = TaskRefs.decode(TaskRefs.encodeIds(tasks), TaskRefs.encodeContents(tasks))

        assertEquals(tasks, decoded)
    }

    @Test
    fun `no tasks encode to nothing and decode to an empty list`() {
        assertNull(TaskRefs.encodeIds(emptyList()))
        assertNull(TaskRefs.encodeContents(emptyList()))
        assertEquals(emptyList<TaskRef>(), TaskRefs.decode(null, null))
    }

    @Test
    fun `a single old style task still reads as one task`() {
        assertEquals(listOf(TaskRef("9", "Email Sam")), TaskRefs.decode("9", "Email Sam"))
    }

    @Test
    fun `display joins the titles on one line`() {
        assertEquals("Write chapter, part one, Review notes", TaskRefs.display(TaskRefs.encodeContents(tasks)))
    }

    @Test
    fun `merging keeps the earlier tasks and skips ones already there`() {
        val merged = TaskRefs.merge(listOf(TaskRef("1", "A")), listOf(TaskRef("1", "A"), TaskRef("3", "C")))

        assertEquals(listOf("1", "3"), merged.map { it.id })
    }
}
