package com.travelblock.app.data.todoist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultTodoistRepositoryTest {
    @Test fun connectsOnlyAfterValidationAndUsesV1CloseEndpoint() = runTest {
        val store = FakeTokenStore()
        val http = FakeHttp(
            listOf(
                TodoistHttpResponse(200, """{"results":[],"next_cursor":null}"""),
                TodoistHttpResponse(204, ""),
            ),
        )
        val repository = DefaultTodoistRepository(store, http)
        assertTrue(repository.connect("secret-test-value") is TodoistResult.Success)
        assertEquals("secret-test-value", store.value.value)
        assertTrue(repository.completeTask("abc") is TodoistResult.Success)
        assertTrue(repository.completeTask("abc") is TodoistResult.Success)
        assertEquals("tasks?limit=1", http.calls[0].first)
        assertEquals(2, http.calls.size)
        assertEquals("tasks/abc/close", http.calls[1].first)
        assertEquals("POST", http.calls[1].third)
    }

    @Test fun unauthorizedValidationDoesNotPersistToken() = runTest {
        val store = FakeTokenStore()
        val repository = DefaultTodoistRepository(store, FakeHttp(listOf(TodoistHttpResponse(401, ""))))
        val result = repository.connect("invalid")
        assertEquals("token invalid", (result as TodoistResult.Failure).message)
        assertEquals(null, store.value.value)
    }

    private class FakeTokenStore : TodoistTokenStore {
        val value = MutableStateFlow<String?>(null)
        override val token: Flow<String?> = value
        override suspend fun save(token: String) { value.value = token }
        override suspend fun clear() { value.value = null }
    }

    private class FakeHttp(responses: List<TodoistHttpResponse>) : TodoistHttpClient {
        private val pending = responses.toMutableList()
        val calls = mutableListOf<Triple<String, String, String>>()
        override suspend fun request(request: TodoistHttpRequest): TodoistHttpResponse {
            calls += Triple(request.path, request.token, request.method)
            return pending.removeAt(0)
        }
    }
}
