package com.workernotfound.app.feature.mypage.data

import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.mypage.data.remote.UpdateMemberNameDto
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MemberProfileContractTest {
    @Test fun loadsNullablePhoneAndUpdatesOnlyTheSupportedNameField() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val original = """{"success":true,"data":{"memberId":4,"name":"Worker","email":"worker@example.com","role":"WORKER","phoneNumber":null,"workerProfile":{}}}"""
            server.enqueue(MockResponse().setBody(original))
            server.enqueue(MockResponse().setBody(original.replace("Worker", "민주")))
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(MemberProfileApiService::class.java)
            val profile = api.get().requireData()
            assertNull(profile.phoneNumber)
            assertNull(profile.ownerProfile)
            assertEquals("WORKER", profile.role)
            assertEquals("GET", server.takeRequest().method)
            assertEquals("민주", api.updateName(UpdateMemberNameDto("민주")).requireData().name)
            val update = server.takeRequest()
            assertEquals("PATCH", update.method)
            assertEquals("/api/members/me", update.path)
            assertEquals("""{"name":"민주"}""", update.body.readUtf8())
        } finally { server.shutdown() }
    }
}
