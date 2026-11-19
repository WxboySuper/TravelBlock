package com.travelblock.app.data.todoist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.net.URLEncoder

interface TodoistRepository {
    val isConnected: Flow<Boolean>
    suspend fun connect(token: String): TodoistResult<Unit>
    suspend fun disconnect()
    suspend fun loadTasks(): TodoistResult<List<TodoistTask>>
    suspend fun completeTask(id: String): TodoistResult<Unit>
}

class DefaultTodoistRepository(
    private val tokenStore: TodoistTokenStore,
    private val http: TodoistHttpClient,
) : TodoistRepository {
    override val isConnected: Flow<Boolean> = tokenStore.token.map { !it.isNullOrBlank() }
    @Volatile private var cachedTasks: List<TodoistTask> = emptyList()
    private val completedTaskIds = mutableSetOf<String>()

    override suspend fun connect(token: String): TodoistResult<Unit> {
        val cleanToken = token.trim()
        if (cleanToken.isEmpty()) return TodoistResult.Failure("Paste your Todoist token to connect.")
        return try {
            val response = http.request(TodoistHttpRequest("tasks?limit=1", cleanToken, "GET"))
            if (response.code !in 200..299) return TodoistResult.Failure(mapTodoistStatus(response.code))
            TodoistParser.parseTasks(response.body)
            tokenStore.save(cleanToken)
            TodoistResult.Success(Unit)
        } catch (error: Exception) {
            TodoistResult.Failure(mapTodoistFailure(error))
        }
    }

    override suspend fun disconnect() {
        tokenStore.clear()
        cachedTasks = emptyList()
        completedTaskIds.clear()
    }

    override suspend fun loadTasks(): TodoistResult<List<TodoistTask>> {
        val token = tokenStore.token.first()
            ?: return TodoistResult.Failure("Connect Todoist in Settings.")
        return try {
            val projectsResponse = http.request(TodoistHttpRequest("projects", token, "GET"))
            val allResponse = http.request(TodoistHttpRequest("tasks?limit=50", token, "GET"))
            if (projectsResponse.code !in 200..299) return TodoistResult.Failure(mapTodoistStatus(projectsResponse.code))
            if (allResponse.code !in 200..299) return TodoistResult.Failure(mapTodoistStatus(allResponse.code))
            val projects = TodoistParser.parseProjects(projectsResponse.body)
            val all = TodoistParser.parseTasks(allResponse.body, projects).tasks
            val duePath = taskFilterPath("today | overdue")
            val dueResponse = http.request(TodoistHttpRequest(duePath, token, "GET"))
            if (dueResponse.code !in 200..299) return TodoistResult.Failure(mapTodoistStatus(dueResponse.code))
            val dueIds = TodoistParser.parseTasks(dueResponse.body, projects).tasks.map { it.id }.toSet()
            val sorted = all.distinctBy { it.id }.sortedWith(
                compareByDescending<TodoistTask> { it.id in dueIds }.thenBy { it.content.lowercase() },
            ).take(MAX_TASKS)
            cachedTasks = sorted
            TodoistResult.Success(sorted)
        } catch (error: Exception) {
            if (cachedTasks.isNotEmpty()) TodoistResult.Success(cachedTasks)
            else TodoistResult.Failure(mapTodoistFailure(error))
        }
    }

    override suspend fun completeTask(id: String): TodoistResult<Unit> {
        if (id in completedTaskIds) return TodoistResult.Success(Unit)
        val token = tokenStore.token.first()
            ?: return TodoistResult.Failure("Connect Todoist in Settings.")
        return try {
            val encodedId = URLEncoder.encode(id, Charsets.UTF_8.name())
            val response = http.request(TodoistHttpRequest("tasks/$encodedId/close", token, "POST"))
            if (response.code == 204 || response.code in 200..299) {
                completedTaskIds += id
                TodoistResult.Success(Unit)
            }
            else TodoistResult.Failure(mapTodoistStatus(response.code))
        } catch (error: Exception) {
            TodoistResult.Failure(mapTodoistFailure(error))
        }
    }

    companion object {
        private const val MAX_TASKS = 50
    }
}
