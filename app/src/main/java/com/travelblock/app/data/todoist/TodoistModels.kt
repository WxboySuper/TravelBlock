package com.travelblock.app.data.todoist

data class TodoistTask(
    val id: String,
    val content: String,
    val projectName: String?,
    val dueLabel: String?,
    val priority: Int,
)

sealed interface TodoistResult<out T> {
    data class Success<T>(val value: T) : TodoistResult<T>
    data class Failure(val message: String) : TodoistResult<Nothing>
}

interface TodoistTokenStore {
    val token: kotlinx.coroutines.flow.Flow<String?>
    suspend fun save(token: String)
    suspend fun clear()
}

fun interface TodoistHttpClient {
    suspend fun request(request: TodoistHttpRequest): TodoistHttpResponse
}

data class TodoistHttpRequest(val path: String, val token: String, val method: String)
data class TodoistHttpResponse(val code: Int, val body: String)
