package com.travelblock.app.data.todoist

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoistApi(private val baseUrl: String = API_BASE_URL) : TodoistHttpClient {
    override suspend fun request(request: TodoistHttpRequest): TodoistHttpResponse =
        withContext(Dispatchers.IO) {
            val connection = URL(baseUrl + request.path).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = request.method
                connection.connectTimeout = TIMEOUT_MILLIS
                connection.readTimeout = TIMEOUT_MILLIS
                connection.setRequestProperty("Authorization", "Bearer ${request.token}")
                connection.setRequestProperty("Accept", "application/json")
                val code = connection.responseCode
                val stream = if (code < 400) connection.inputStream else connection.errorStream
                TodoistHttpResponse(code, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
            } finally {
                connection.disconnect()
            }
        }

    companion object {
        const val API_BASE_URL = "https://api.todoist.com/api/v1/"
        private const val TIMEOUT_MILLIS = 10_000
    }
}

data class ParsedTasks(val tasks: List<TodoistTask>, val nextCursor: String?)

object TodoistParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseTasks(body: String, projects: Map<String, String> = emptyMap()): ParsedTasks {
        val root = json.parseToJsonElement(body).jsonObject
        val entries = root["results"]?.jsonArray ?: JsonArray(emptyList())
        val tasks = entries.mapNotNull { element -> element.toTask(projects) }
        val cursor = root["next_cursor"]?.takeUnless { it.toString() == "null" }
            ?.jsonPrimitive?.content
        return ParsedTasks(tasks, cursor)
    }

    fun parseProjects(body: String): Map<String, String> {
        val entries = json.parseToJsonElement(body).jsonObject["results"]?.jsonArray ?: return emptyMap()
        return entries.mapNotNull { element ->
            val project = element.jsonObject
            val id = project.stringValue("id") ?: return@mapNotNull null
            val name = project.stringValue("name") ?: return@mapNotNull null
            id to name
        }.toMap()
    }

    private fun kotlinx.serialization.json.JsonElement.toTask(projects: Map<String, String>): TodoistTask? {
        val task = runCatching { jsonObject }.getOrNull() ?: return null
        val id = task.stringValue("id") ?: return null
        val content = task.stringValue("content") ?: return null
        val due = task["due"]?.takeUnless { it.toString() == "null" }
        val dueObject = due?.let { runCatching { it.jsonObject }.getOrNull() }
        val projectId = task.stringValue("project_id")
        return TodoistTask(
            id = id,
            content = content,
            projectName = projectId?.let(projects::get),
            dueLabel = dueObject?.let { it.stringValue("string") ?: it.stringValue("date") },
            priority = task["priority"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
        )
    }

    private fun JsonObject.stringValue(key: String): String? =
        this[key]?.takeUnless { it.toString() == "null" }?.jsonPrimitive?.content
}

fun taskFilterPath(query: String, cursor: String? = null, limit: Int = 50): String {
    val params = buildList {
        add("query=" + URLEncoder.encode(query, Charsets.UTF_8.name()))
        add("limit=$limit")
        cursor?.let { add("cursor=" + URLEncoder.encode(it, Charsets.UTF_8.name())) }
    }
    return "tasks/filter?${params.joinToString("&")}" 
}

fun mapTodoistFailure(error: Throwable): String = when (error) {
    is IOException -> "offline"
    else -> "Something went wrong. Try again."
}

fun mapTodoistStatus(code: Int): String = when (code) {
    401, 403 -> "token invalid"
    429 -> "rate limited, try later"
    else -> "Todoist returned an error ($code)."
}
