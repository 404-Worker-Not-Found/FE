package com.workernotfound.app.feature.auth

import com.google.gson.JsonParser
import com.workernotfound.app.feature.auth.data.remote.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegistrationContractTest {
    @Test fun verificationUsesSeparateTargetsAndResetPurpose() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(AuthApiService::class.java)
            repeat(5) { server.enqueue(MockResponse().setBody("""{"success":true,"data":{"verified":false}}""")) }
            api.sendEmail(EmailDto("worker@example.com")).requireData()
            assertEquals("/api/auth/email-verifications/send", server.takeRequest().path)
            assertFalse(api.verifyEmail(EmailCodeDto("worker@example.com", "123456")).requireData().verified)
            val emailRequest = server.takeRequest()
            assertEquals("/api/auth/email-verifications/verify", emailRequest.path)
            assertEquals("123456", JsonParser.parseString(emailRequest.body.readUtf8()).asJsonObject["verificationCode"].asString)
            api.sendSms(PhoneDto("01000000000"))
            assertEquals("/api/auth/sms-verifications/send", server.takeRequest().path)
            api.verifySms(PhoneCodeDto("01000000000", "123456"))
            val smsRequest = server.takeRequest()
            assertEquals("/api/auth/sms-verifications/verify", smsRequest.path)
            assertTrue(JsonParser.parseString(smsRequest.body.readUtf8()).asJsonObject.has("phoneNumber"))
            api.sendReset(EmailDto("worker@example.com"))
            assertEquals("/api/auth/password-resets/email-verifications/send", server.takeRequest().path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":null}"""))
            assertTrue(api.resetPassword(PasswordResetDto("worker@example.com", "123456", "synthetic-password")).success)
            val resetRequest = server.takeRequest()
            assertEquals("/api/auth/password-resets", resetRequest.path)
            assertEquals(setOf("email", "verificationCode", "newPassword"), JsonParser.parseString(resetRequest.body.readUtf8()).asJsonObject.keySet())
        } finally { server.shutdown() }
    }
    @Test fun signupUsesRoleSpecificEndpointAndTokenEnvelope() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(AuthApiService::class.java)
            val response = """{"success":true,"data":{"memberId":17,"role":"OWNER","tokenResponse":{"accessToken":"synthetic-access","refreshToken":"synthetic-refresh"}}}"""
            server.enqueue(MockResponse().setBody(response))
            val location = LocationDto("서울", "37.5".toBigDecimal(), "127.0".toBigDecimal())
            val session = api.signupOwner(OwnerSignupDto("점주", "owner@example.com", "synthetic-password", "01000000000", "device", "0000000000", "가게", "카페", location)).requireData()
            assertEquals("OWNER", session.role)
            assertEquals("synthetic-access", session.tokenResponse.accessToken)
            val request = server.takeRequest()
            assertEquals("/api/auth/signup/owner", request.path)
            val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
            assertTrue(body.has("storeLocation"))
            assertFalse(body.has("businessId"))
            server.enqueue(MockResponse().setBody(response.replace("OWNER", "WORKER")))
            api.signupWorker(WorkerSignupDto("근로자", "worker@example.com", "synthetic-password", "01000000000", "device", 10320, 3, false, location, listOf("카페"), listOf(AvailableTimeDto("MONDAY", "09:00", "12:00"))))
            val workerRequest = server.takeRequest()
            assertEquals("/api/auth/signup/worker", workerRequest.path)
            val worker = JsonParser.parseString(workerRequest.body.readUtf8()).asJsonObject
            assertTrue(worker.has("baseLocation"))
            assertEquals("MONDAY", worker["availableTimes"].asJsonArray[0].asJsonObject["dayOfWeek"].asString)
        } finally { server.shutdown() }
    }
}
