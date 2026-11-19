package com.travelblock.app.domain.model

/** One task a flight is flown for. */
data class TaskRef(val id: String, val content: String)

/**
 * A flight can be flown for several tasks. They travel in the single task id and content fields every layer
 * already stores: ids joined by commas, contents joined by a separator that never appears in a task title.
 */
object TaskRefs {
    private const val ID_SEPARATOR = ","
    private const val CONTENT_SEPARATOR = "\u001F"

    fun encodeIds(tasks: List<TaskRef>): String? = tasks.takeIf { it.isNotEmpty() }?.joinToString(ID_SEPARATOR) { it.id }

    fun encodeContents(tasks: List<TaskRef>): String? = tasks.takeIf { it.isNotEmpty() }?.joinToString(CONTENT_SEPARATOR) { it.content }

    fun decode(ids: String?, contents: String?): List<TaskRef> {
        if (ids.isNullOrBlank() || contents == null) return emptyList()
        return ids.split(ID_SEPARATOR).zip(contents.split(CONTENT_SEPARATOR)).map { (id, content) -> TaskRef(id, content) }
    }

    /** The task titles as one readable line, for places that just show what you flew for. */
    fun display(contents: String?): String? = contents?.split(CONTENT_SEPARATOR)?.joinToString(", ")?.takeIf { it.isNotBlank() }

    /** [previous] followed by the [added] tasks that are not already there. */
    fun merge(previous: List<TaskRef>, added: List<TaskRef>): List<TaskRef> = previous + added.filter { new -> previous.none { it.id == new.id } }
}
