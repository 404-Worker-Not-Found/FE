package com.workernotfound.app.feature.cancellation

import com.workernotfound.app.feature.cancellation.data.*
import com.workernotfound.app.feature.cancellation.domain.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CancellationContractTest {
    @Test fun cancellationRequestsAndDecisionsUseParticipantEndpoints() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(CancellationApi::class.java)
            fun response(status: String) = MockResponse().setBody("""{"success":true,"data":{"requestId":7,"workId":9,"requesterMemberId":4,"reason":"schedule","status":"$status","createdAt":"2026-11-02T10:00:00"}}""")
            server.enqueue(response("PENDING"))
            assertEquals("PENDING",api.request(9,"same-retry-key",CancellationBody("schedule")).requireData().toDomain(9).status)
            val sent = server.takeRequest()
            assertEquals("/api/works/me/9/cancellations",sent.path)
            assertEquals("same-retry-key",sent.getHeader("Idempotency-Key"))
            assertEquals("""{"reason":"schedule"}""",sent.body.readUtf8())
            server.enqueue(response("APPROVED")); api.approve(9,7).requireData().toDomain(9,7,"APPROVED")
            assertEquals("/api/works/me/9/cancellations/7/approve",server.takeRequest().path)
            server.enqueue(response("REJECTED")); api.reject(9,7).requireData().toDomain(9,7,"REJECTED")
            assertEquals("/api/works/me/9/cancellations/7/reject",server.takeRequest().path)
            server.enqueue(response("WITHDRAWN")); api.withdraw(9,7).requireData().toDomain(9,7,"WITHDRAWN")
            assertEquals("/api/works/me/9/cancellations/7/withdraw",server.takeRequest().path)
        } finally { server.shutdown() }
    }
    @Test fun onlyCounterpartRespondsAndOnlyRequesterWithdrawsBeforeStart() {
        val row = Cancellation(7,9,4,"schedule","PENDING","2026-11-02T10:00:00")
        assertTrue(row.canWithdraw(4)); assertFalse(row.canRespond(4))
        assertFalse(row.canWithdraw(5)); assertTrue(row.canRespond(5))
        assertFalse(row.copy(status="APPROVED").canRespond(5))
        val context = CancellationContext(4,"store","job",true,false,true,1000,emptyList())
        assertTrue(context.canRequest(999)); assertFalse(context.canRequest(1000))
        assertFalse(context.copy(checkedIn=true).canRequest(999))
        assertFalse(context.copy(confirmed=false).canRequest(999))
        assertFalse(context.copy(requests=listOf(row)).canRequest(999))
    }
    @Test fun mismatchingOrExpiredReceiptCannotBecomeApproval() {
        val expired = CancellationDto(7,9,4,"schedule","EXPIRED","2026-11-02T10:00:00")
        assertTrue(runCatching { expired.toDomain(9,7,"APPROVED") }.isFailure)
        assertTrue(runCatching { expired.toDomain(8) }.isFailure)
        assertTrue(runCatching { expired.toDomain(9,8) }.isFailure)
    }
}
