package com.workernotfound.app.feature.review

import com.workernotfound.app.feature.review.data.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReviewContractTest {
    @Test fun serverDeadlineAndAnonymousReceivedReviewsRemainUnchanged() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(ReviewApi::class.java)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"works":[{"workId":9,"jobPostId":3,"revieweeMemberId":2,"workDate":"2026-11-02","completedAt":"2026-11-02T12:00:00","writableUntil":"2026-11-16T12:00:00"}],"page":0,"size":20,"totalCount":1}}"""))
            assertEquals("2026-11-16T12:00:00", api.pending(0).requireData().works.single().writableUntil)
            assertEquals("/api/members/me/reviews/pending?page=0&size=20", server.takeRequest().path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"memberId":4,"reviewCount":1,"averageRating":4.5,"reviews":[{"reviewId":1,"reviewerRole":"OWNER","rating":5,"comment":null,"createdAt":"2026-11-02T13:00:00"}],"page":0,"size":20,"totalCount":1}}"""))
            val received = api.received(4, 0).requireData()
            assertEquals("4.5", received.averageRating?.toPlainString())
            assertNull(received.reviews.single().toDomain().comment)
            assertEquals("/api/members/4/reviews?page=0&size=20", server.takeRequest().path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"reviewId":1,"workId":9,"revieweeMemberId":2,"rating":5,"comment":"good"}}"""))
            confirmReview(api.write(WriteReviewDto(9,5,"good")).requireData(),9,2,5,"good")
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/members/me/reviews", request.path)
            assertEquals("""{"workId":9,"rating":5,"comment":"good"}""", request.body.readUtf8())
        } finally { server.shutdown() }
    }
    @Test fun wrongWorkOrCounterpartCannotBecomeSuccessfulReview() {
        val response = WrittenReviewDto(1,9,2,5,null)
        confirmReview(response,9,2,5,null)
        assertTrue(runCatching { confirmReview(response,8,2,5,null) }.isFailure)
        assertTrue(runCatching { confirmReview(response,9,3,5,null) }.isFailure)
        assertTrue(runCatching { confirmReview(response,9,2,4,null) }.isFailure)
    }
    @Test fun pageEnvelopeUsesTotalCountAndRejectsNonProgressingEmptyPages() {
        assertEquals(1, nextReviewPage(0,0,20,21,20))
        assertNull(nextReviewPage(1,1,20,21,1))
        assertNull(nextReviewPage(0,0,20,0,0))
        assertTrue(runCatching { nextReviewPage(0,0,20,21,0) }.isFailure)
        assertTrue(runCatching { nextReviewPage(0,1,20,21,1) }.isFailure)
    }
}
