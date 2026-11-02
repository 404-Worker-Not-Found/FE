package com.workernotfound.app.feature.account

import com.workernotfound.app.feature.account.data.*
import com.workernotfound.app.feature.account.domain.*
import com.workernotfound.app.feature.account.ui.AccountUiState
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AccountContractTest {
    @Test fun contactChangeUsesAccountVerificationAndStableIdempotencyKey() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val api = api(server)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":null}"""))
            assertTrue(api.phone(PhoneTarget("01000000000")).success)
            assertEquals("/api/auth/account/sms-verifications/send", server.takeRequest().path)
            val key = "b912ae42-0461-48f7-b7a1-499ab606c1f2"
            server.enqueue(MockResponse().setResponseCode(202).setBody("""{"success":true,"data":{"commandId":"$key","status":"PENDING"}}"""))
            val pending = api.change(key, ContactChangeBody("PHONE", "01000000000", "123456")).requireData().toDomain(key)
            assertFalse(pending.terminal)
            val request = server.takeRequest()
            assertEquals(key, request.getHeader("Idempotency-Key"))
            assertEquals("/api/auth/account/contact-changes", request.path)
            assertTrue(request.body.readUtf8().contains("\"verificationCode\":\"123456\""))
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"commandId":"$key","status":"SUCCEEDED"}}"""))
            assertTrue(api.contact(key).requireData().toDomain(key).terminal)
            assertEquals("/api/auth/account/contact-changes/$key", server.takeRequest().path)
        } finally { server.shutdown() }
    }
    @Test fun withdrawalChecksServerStateRatherThanTreatingAcceptedAsCompletion() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val key = "b912ae42-0461-48f7-b7a1-499ab606c1f2"
            val api = api(server)
            server.enqueue(MockResponse().setResponseCode(202).setBody("""{"success":true,"data":{"commandId":"$key","state":"CHECKING","blockedService":null}}"""))
            assertFalse(api.withdraw(key).requireData().toDomain(key).terminal)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals(key, request.getHeader("Idempotency-Key"))
            assertEquals("/api/auth/account/withdrawals", request.path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"commandId":"$key","state":"REJECTED"}}"""))
            assertEquals("REJECTED", api.withdrawal(key).requireData().toDomain(key).status)
            assertEquals("/api/auth/account/withdrawals/$key", server.takeRequest().path)
            assertTrue(runCatching { WithdrawalDto("other", "SUCCEEDED").toDomain(key) }.isFailure)
        } finally { server.shutdown() }
    }
    @Test fun contactCodesNeverAppearInDiagnosticState() {
        val state = AccountUiState(target = "test@example.invalid", code = "123456")
        assertTrue(state.validTarget.not()) // PHONE is the default channel.
        assertTrue(state.copy(channel = ContactChannel.EMAIL).validTarget)
        assertFalse(state.toString().contains("123456"))
        assertFalse(state.toString().contains("test@example.invalid"))
    }
    private fun api(server: MockWebServer) = Retrofit.Builder().baseUrl(server.url("/"))
        .addConverterFactory(GsonConverterFactory.create()).build().create(AccountApi::class.java)
}
