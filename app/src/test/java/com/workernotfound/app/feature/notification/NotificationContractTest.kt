package com.workernotfound.app.feature.notification

import com.workernotfound.app.feature.notification.data.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationContractTest {
    @Test fun cursorEnvelopeNullableTargetsAndReadEndpointMatchBackend() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(NotificationApi::class.java)
            val unread = """{"notificationId":8,"type":"WorkStarted","jobPostId":2,"applicationId":null,"workId":4,"occurredAt":"2026-11-02T10:00:00","readAt":null}"""
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"content":[$unread],"nextBeforeId":8,"hasNext":true}}"""))
            val page = api.page(12).requireData().toDomain(12)
            assertEquals(8L, page.next)
            assertNull(page.items.single().applicationId)
            assertFalse(page.items.single().read)
            assertEquals("/api/notifications/me?beforeId=12&size=20", server.takeRequest().path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"unreadCount":3}}"""))
            assertEquals(3, api.unread().requireData().unreadCount.toInt())
            assertEquals("/api/notifications/me/unread-count", server.takeRequest().path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":${unread.replace("\"readAt\":null", "\"readAt\":\"2026-11-02T11:00:00\"")}}"""))
            assertTrue(api.read(8).requireData().toDomain().read)
            val request = server.takeRequest()
            assertEquals("PATCH", request.method)
            assertEquals("/api/notifications/me/8/read", request.path)
        } finally { server.shutdown() }
    }
    @Test fun aNonAdvancingOrMissingCursorCannotLoopForever() {
        val row = NotificationDto(8, "MatchConfirmed", 2, 3, null, "2026-11-02T10:00:00", null)
        assertTrue(runCatching { NotificationPageDto(listOf(row), 12, true).toDomain(12) }.isFailure)
        assertTrue(runCatching { NotificationPageDto(emptyList(), 8, true).toDomain(12) }.isFailure)
        assertTrue(runCatching { NotificationPageDto(listOf(row), null, true).toDomain(12) }.isFailure)
        assertNull(NotificationPageDto(listOf(row), 8, false).toDomain(12).next)
    }
}
