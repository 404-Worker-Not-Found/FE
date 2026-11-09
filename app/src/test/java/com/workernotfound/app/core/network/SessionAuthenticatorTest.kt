package com.workernotfound.app.core.network

import com.workernotfound.app.core.datastore.SessionStorage
import com.workernotfound.app.core.datastore.StoredSession
import com.workernotfound.app.feature.auth.data.remote.AuthApiService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SessionAuthenticatorTest {
    private val server = MockWebServer()
    private val initial = StoredSession(1, "WORKER", "old-access", "old-refresh")
    private val store = FakeSessionStore(initial)
    private lateinit var client: OkHttpClient

    @Before fun setup() {
        server.start()
        val api = Retrofit.Builder().baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create()).build().create(AuthApiService::class.java)
        client = OkHttpClient.Builder().addInterceptor(SessionInterceptor(store))
            .authenticator(SessionAuthenticator(store, api)).build()
    }
    @After fun teardown() { server.shutdown() }

    @Test fun rotatesBothTokensAndRetriesOnlyOnce() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(tokens())
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))
        client.newCall(Request.Builder().url(server.url("/protected")).build()).execute().use { assertEquals(200, it.code) }
        assertEquals("Bearer old-access", server.takeRequest().getHeader("Authorization"))
        val reissue = server.takeRequest()
        assertEquals("/api/auth/tokens/reissue", reissue.path)
        assertNull(reissue.getHeader("Authorization"))
        assertTrue(reissue.body.readUtf8().contains("old-refresh"))
        assertEquals("Bearer new-access", server.takeRequest().getHeader("Authorization"))
        assertEquals("new-refresh", store.read()?.refreshToken)
    }

    @Test fun secondUnauthorizedDoesNotLoop() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(tokens())
        server.enqueue(MockResponse().setResponseCode(401))
        client.newCall(Request.Builder().url(server.url("/protected")).build()).execute().use { assertEquals(401, it.code) }
        assertEquals(3, server.requestCount)
    }

    @Test fun rejectedRefreshClearsSession() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))
        client.newCall(Request.Builder().url(server.url("/protected")).build()).execute().close()
        assertNull(store.read())
    }

    @Test fun serverFailurePreservesRefreshSession() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(503))
        client.newCall(Request.Builder().url(server.url("/protected")).build()).execute().close()
        assertEquals(initial, store.read())
    }

    @Test fun logoutDuringRefreshCannotResurrectSession() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(tokens())
        store.beforeReplace = { store.clear() }
        client.newCall(Request.Builder().url(server.url("/protected")).build()).execute().use { assertEquals(401, it.code) }
        assertNull(store.read())
        assertEquals(2, server.requestCount)
    }

    @Test fun loginDuringRefreshCannotOverwriteAnotherAccount() {
        val replacement = StoredSession(2, "OWNER", "owner-access", "owner-refresh")
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(tokens())
        store.beforeReplace = { store.save(replacement) }
        client.newCall(Request.Builder().url(server.url("/protected")).build()).execute().close()
        assertEquals(replacement, store.read())
        assertEquals(2, server.requestCount)
    }

    @Test fun anotherAccountBeforeAuthenticationCannotRetryOriginalMutation() {
        store.save(StoredSession(2, "OWNER", "owner-access", "owner-refresh"))
        val originalRequest = Request.Builder().url(server.url("/api/applications"))
            .header("Authorization", "Bearer old-access")
            .tag(RequestMember::class.java, RequestMember(1)).build()
        val response = okhttp3.Response.Builder().request(originalRequest)
            .protocol(okhttp3.Protocol.HTTP_1_1).code(401).message("Unauthorized").build()
        assertNull(client.authenticator.authenticate(null, response))
        assertEquals(0, server.requestCount)
    }

    @Test fun anonymous401NeverRefreshes() {
        store.clear()
        server.enqueue(MockResponse().setResponseCode(401))
        client.newCall(Request.Builder().url(server.url("/protected")).build()).execute().close()
        assertNull(server.takeRequest().getHeader("Authorization"))
        assertEquals(1, server.requestCount)
    }

    private fun tokens() = MockResponse().setHeader("Content-Type", "application/json").setBody(
        """{"success":true,"data":{"accessToken":"new-access","refreshToken":"new-refresh"}}""",
    )

    private class FakeSessionStore(private var current: StoredSession?) : SessionStorage {
        var beforeReplace: (() -> Unit)? = null
        override fun deviceId() = "test-device"
        override fun read() = current
        override fun save(session: StoredSession) { current = session }
        override fun clear() { current = null }
        override fun replace(expected: StoredSession, updated: StoredSession): Boolean {
            beforeReplace?.invoke()
            if (current != expected) return false
            current = updated
            return true
        }
        override fun clearIfCurrent(expected: StoredSession) { if (current == expected) clear() }
    }
}
