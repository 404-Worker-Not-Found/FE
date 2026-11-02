package com.workernotfound.app.feature.worker.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class WorkerJobContractTest {
    @Test fun searchUsesFlatQueryParametersAndActualResponseEnvelope() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"jobs":[{"id":42,"storeName":"store","categoryName":"cafe","workDate":"2026-11-01","startTime":"18:00:00","endTime":"22:00:00","baseHourlyWage":10320,"extraWage":500,"remainingMinutes":20,"distanceKm":null,"isUrgent":true,"status":"OPEN"}],"totalCount":1,"totalPages":1}}"""))
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(WorkerJobApiService::class.java)
            val data = api.search(mapOf("type" to "URGENT", "page" to "0", "minWage" to "10320")).requireData()
            assertEquals(42L, data.jobs.single().id)
            assertNull(data.jobs.single().distanceKm)
            assertTrue(data.jobs.single().isUrgent)
            val request = server.takeRequest().requestUrl!!
            assertEquals("URGENT", request.queryParameter("type"))
            assertNull(request.queryParameter("request"))
        } finally { server.shutdown() }
    }

    @Test fun countdownUsesKoreaTimeAndClampsPastShifts() {
        val clock = Clock.fixed(Instant.parse("2026-11-01T08:00:00Z"), ZoneOffset.UTC)
        assertEquals(3600L, startsInSeconds("2026-11-01", "18:00:00", clock))
        assertEquals(0L, startsInSeconds("2026-11-01", "16:00:00", clock))
    }
}
