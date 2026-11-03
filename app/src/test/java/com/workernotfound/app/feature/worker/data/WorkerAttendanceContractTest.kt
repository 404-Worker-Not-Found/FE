package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.data.remote.WorkCheckInRequestDto
import com.workernotfound.app.feature.worker.data.remote.WorkResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkerWorkApiService
import com.workernotfound.app.feature.worker.domain.model.WorkAttendance
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WorkerAttendanceContractTest {
    private val rules = WorkAttendance("SCHEDULED", 37.0, 127.0, 100.0, 1000L, 5000L, false, "Asia/Seoul")

    @Test fun checkInTimeUsesInclusiveServerBoundaries() {
        assertFalse(rules.allowsTime(999))
        assertTrue(rules.allowsTime(1000))
        assertTrue(rules.allowsTime(5000))
        assertFalse(rules.allowsTime(5001))
    }

    @Test fun distanceMatchesServerRadiusRuleAndMissingTargetIsUnknown() {
        assertEquals(0.0, requireNotNull(rules.distanceFrom(37.0, 127.0)), 0.0001)
        assertTrue(requireNotNull(rules.distanceFrom(37.0008, 127.0)) < rules.radiusMeters)
        assertTrue(requireNotNull(rules.distanceFrom(37.0009, 127.0)) > rules.radiusMeters)
        assertNull(rules.copy(latitude = null).distanceFrom(37.0, 127.0))
    }

    @Test fun unconfirmedOrWrongWorkResponseCannotBecomeAttendanceSuccess() {
        val response = work(null)
        assertTrue(runCatching { confirmedCheckInReceipt("9", response) }.isFailure)
        assertTrue(runCatching { confirmedCheckInReceipt("10", work("2026-11-02T10:00:00")) }.isFailure)
        assertEquals("CHECKED_IN", confirmedCheckInReceipt("9", work("2026-11-02T10:00:00")).status)
    }

    @Test fun sendsOnlyActualCoordinatesToPublicCheckInEndpoint() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"workId":9,"jobPostId":3,"workDate":"2026-11-02","startTime":"10:00:00","endTime":"12:00:00","endTimeNextDay":false,"status":"CHECKED_IN","workerMemberId":4,"checkedInAt":"2026-11-02T10:00:00","startedAt":null}}"""))
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(WorkerWorkApiService::class.java)
            val receipt = confirmedCheckInReceipt("9", api.checkIn(9, WorkCheckInRequestDto(37.0, 127.0)).requireData())
            assertEquals("9", receipt.workId)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/works/me/9/check-in", request.path)
            assertEquals("""{"latitude":37.0,"longitude":127.0}""", request.body.readUtf8())
        } finally { server.shutdown() }
    }

    private fun work(checkedInAt: String?) = WorkResponseDto(9, 3, "2026-11-02", "10:00", "12:00", false,
        "CHECKED_IN", 4, checkedInAt, null)
}
