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

    /** member-service rejects more than 7 fraction digits; auth-service then answers a generic 502. */
    @Test fun signupCoordinatesAreSentWithAtMostSevenFractionDigits() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(AuthApiService::class.java)
            // Typical Double coordinates from Geocoder/map taps.
            val location = signupLocation("서울", 37.556123399999995.toBigDecimal(), 126.92345678912345.toBigDecimal())
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"memberId":17,"role":"OWNER","tokenResponse":{"accessToken":"a","refreshToken":"r"}}}"""))
            api.signupOwner(OwnerSignupDto("점주", "owner@example.com", "synthetic-password", "01000000000", "device", "0000000000", "가게", "카페", location))
            val sent = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject["storeLocation"].asJsonObject
            assertEquals("37.5561234", sent["latitude"].asBigDecimal.toPlainString())
            assertEquals("126.9234568", sent["longitude"].asBigDecimal.toPlainString())
            assertTrue(sent["latitude"].asBigDecimal.scale() <= 7 && sent["longitude"].asBigDecimal.scale() <= 7)
            // Integer-degree values stay plain decimals, never scientific notation.
            val whole = signupLocation("서울", "37".toBigDecimal(), "127".toBigDecimal())
            assertEquals("127.0000000", whole.longitude.toPlainString())
            assertEquals(whole.longitude.toString(), whole.longitude.toPlainString())
        } finally { server.shutdown() }
    }
}
