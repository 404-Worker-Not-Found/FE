package com.workernotfound.app.feature.worker.data

import com.google.gson.Gson
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.history.domain.WorkHistoryEntry
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileDto
import com.workernotfound.app.feature.owner.domain.model.ServerSettlement
import com.workernotfound.app.feature.review.domain.ReceivedReview
import com.workernotfound.app.feature.worker.domain.model.WorkStatus
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerRedesignContractTest {

    @Test fun workerPreferenceReadsMemberServiceWorkerProfile() {
        val json = """
            {"memberId":7,"name":"김알바","email":"a@b.c","phoneNumber":null,"role":"WORKER","ownerProfile":null,
             "workerProfile":{"workerProfileId":3,"desiredHourlyWage":11000,"activityRadiusKm":3,"immediatelyAvailable":true,
               "baseLocation":{"locationId":1,"address":"서울","detailAddress":null,"latitude":37.5012,"longitude":127.0396},
               "preferredBusinessTypes":["카페/음료"],
               "availableTimes":[{"availableTimeId":1,"dayOfWeek":"WEDNESDAY","startTime":"09:00:00","endTime":"18:00:00"},
                                 {"availableTimeId":2,"dayOfWeek":"NOPE","startTime":"09:00","endTime":"10:00"}]}}
        """.trimIndent()
        val preference = Gson().fromJson(json, MemberProfileDto::class.java).toPreference()
        assertEquals("김알바", preference.name)
        assertEquals(3, preference.activityRadiusKm)
        assertEquals(37.5012, preference.baseLatitude!!, 1e-9)
        assertTrue(preference.hasBaseLocation)
        assertEquals(listOf("카페/음료"), preference.preferredBusinessTypes)
        // Unknown day values are dropped rather than guessed.
        assertEquals(1, preference.availableTimes.size)
        assertEquals(DayOfWeek.WEDNESDAY, preference.availableTimes.single().day)
        assertEquals(LocalTime.of(18, 0), preference.availableTimes.single().end)
    }

    @Test(expected = ApiException::class)
    fun ownerProfileIsNotAWorkerPreference() {
        Gson().fromJson("""{"memberId":1,"name":"점주","email":"o@b.c","role":"OWNER"}""", MemberProfileDto::class.java).toPreference()
    }

    @Test fun missingWorkerProfileHasNoBaseLocation() {
        val preference = Gson().fromJson("""{"memberId":1,"name":"알바","email":"w@b.c","role":"WORKER"}""", MemberProfileDto::class.java)
            .toPreference()
        assertFalse(preference.hasBaseLocation)
        assertNull(preference.activityRadiusKm)
    }

    @Test fun onlyLivePendingProposalsShowTheBanner() {
        assertTrue(isValidOffer("APPLIED", "PENDING", 120))
        assertFalse(isValidOffer("APPLIED", "PENDING", 0))
        assertFalse(isValidOffer("APPLIED", "DECLINED", 120))
        assertFalse(isValidOffer("CANCELED", "PENDING", 120))
        assertFalse(isValidOffer("APPLIED", "PENDING", null))
    }

    @Test fun workSectionFollowsServerStatusOnly() {
        assertEquals(WorkStatus.SCHEDULED, workSection("SCHEDULED"))
        assertEquals(WorkStatus.IN_PROGRESS, workSection("CHECKED_IN"))
        assertEquals(WorkStatus.IN_PROGRESS, workSection("IN_PROGRESS"))
    }

    @Test fun trustSummaryJoinsReviewsAndSettlementsByWork() {
        fun work(id: Long, date: String, status: String, amount: String? = null) = WorkHistoryEntry(
            workId = id, jobId = id * 10, storeName = "가게$id", date = date, startTime = "10:00:00", endTime = "14:00:00",
            nextDay = false, status = status, completedAt = null,
            settlement = amount?.let {
                ServerSettlement(id, id, id * 10, BigDecimal(it), "KRW", "PAID", null, false, null, null, null)
            },
        )
        val works = listOf(
            work(1, "2026-11-01", "COMPLETED", "48000"),
            work(2, "2026-11-05", "NO_SHOW"),
            work(3, "2026-11-07", "COMPLETED"),
        )
        val reviews = listOf(
            ReceivedReview(10, "OWNER", 5, "성실해요", "2026-11-02T10:00:00", workId = 1),
            ReceivedReview(11, "OWNER", 3, " ", "2026-11-08T10:00:00", workId = 99),
        )
        val summary = buildTrustSummary(works, reviews, averageRating = null, reviewCount = 2)
        assertNull(summary.score)
        assertNull(summary.noShowRisk)
        assertEquals(2, summary.totalWorkCount)
        assertEquals(1, summary.noShowCount)
        assertEquals(3, summary.finishedCount)
        assertEquals(4.0, summary.averageRating!!, 1e-9)
        // Newest review first; an unknown work has no store name and a blank comment is dropped.
        assertNull(summary.recentReviews.first().storeName)
        assertNull(summary.recentReviews.first().comment)
        assertEquals("가게1", summary.recentReviews.last().storeName)
        // Newest work first, with its own rating and real settlement amount.
        assertEquals(listOf("3", "2", "1"), summary.workHistory.map { it.id })
        assertEquals(5, summary.workHistory.last().rating)
        assertEquals(BigDecimal("48000"), summary.workHistory.last().settlementAmount)
        assertNull(summary.workHistory.first().settlementAmount)
        assertEquals("2026.11.07 (토)", summary.workHistory.first().dateText)
    }
}
