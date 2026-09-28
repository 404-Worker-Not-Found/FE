package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.ApplicantWorkRecord
import com.workernotfound.app.feature.owner.domain.model.NoShowRiskLevel
import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.PastPosting
import com.workernotfound.app.feature.owner.domain.model.PastPostingStatus
import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import com.workernotfound.app.feature.owner.domain.model.SettlementRecord
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Internal mock posting record (richer than the home card model). */
internal data class MockPosting(
    val id: String,
    val workSummary: String,
    val category: String,
    val startMillis: Long,
    val endMillis: Long,
    val hourlyWage: Int,
    val bonusPerHour: Int,
    val status: PostingStatus,
    val isUrgent: Boolean,
    val applicantIds: List<String>,
    val matchedWorkerId: String?,
    val noShowWorkerIds: Set<String> = emptySet(),
)

/** Internal mock worker profile (applicant pool). */
internal data class MockWorker(
    val id: String,
    val name: String,
    val age: Int,
    val rating: Double,
    val etaMinutes: Int,
    val matchScore: Int,
    val noShowRisk: NoShowRiskLevel,
    val experiencedCategories: Set<String>,
    val trustScore: Int,
    val recentWorks: List<ApplicantWorkRecord>,
    val noShowCount: Int,
    val lastNoShowDate: String?,
)

/**
 * Seed data for [OwnerMockStore]. Posting times of the in-progress shifts are
 * relative to app start so the 근무 현황 timer and the 30-minute no-show rule
 * can be demoed without waiting.
 */
internal object OwnerMockSeed {

    private const val MINUTE = 60_000L
    private const val CAFE = "카페"
    private const val RESTAURANT = "음식점"
    private const val CONVENIENCE = "편의점"
    private const val EVENT = "행사/기타"

    fun postings(zone: ZoneId): List<MockPosting> {
        val now = System.currentTimeMillis() / MINUTE * MINUTE
        val today = LocalDate.now(zone)
        fun at(daysAgo: Long, hour: Int) =
            today.minusDays(daysAgo).atTime(LocalTime.of(hour, 0)).atZone(zone).toInstant().toEpochMilli()
        return listOf(
            MockPosting("1", "홀서빙 및 주문 응대", RESTAURANT, at(0, 18), at(0, 22), 12500, 1300,
                PostingStatus.RECRUITING, isUrgent = true, applicantIds = listOf("a1", "a2", "a3", "a4"), matchedWorkerId = null),
            MockPosting("2", "주방 보조 (설거지/재료 손질)", RESTAURANT, at(0, 11), at(0, 15), 11000, 0,
                PostingStatus.RECRUITING, isUrgent = false, applicantIds = listOf("a5", "a2"), matchedWorkerId = null),
            MockPosting("5", "음료 제조 및 매장 정리", CAFE, now - 230 * MINUTE, now + 10 * MINUTE, 12000, 1200,
                PostingStatus.CLOSED, isUrgent = true, applicantIds = listOf("a7", "a3"), matchedWorkerId = "a7"),
            MockPosting("6", "편의점 계산 및 진열 보조", CONVENIENCE, now - 40 * MINUTE, now + 200 * MINUTE, 11800, 1200,
                PostingStatus.CLOSED, isUrgent = true, applicantIds = listOf("a9", "a10"), matchedWorkerId = "a9"),
            MockPosting("7", "행사 물품 정리", EVENT, now - 35 * MINUTE, now + 145 * MINUTE, 12000, 0,
                PostingStatus.CLOSED, isUrgent = false, applicantIds = listOf("a11"), matchedWorkerId = "a11"),
            MockPosting("3", "매장 마감 청소", RESTAURANT, at(1, 22), at(0, 0), 13000, 2000,
                PostingStatus.DONE, isUrgent = false,
                applicantIds = listOf("a6", "a1", "a3", "a4", "a5", "a8"), matchedWorkerId = "a6"),
            MockPosting("4", "오픈 준비 및 진열", CAFE, at(2, 7), at(2, 10), 11500, 0,
                PostingStatus.CLOSED, isUrgent = false, applicantIds = listOf("a8"), matchedWorkerId = "a8",
                noShowWorkerIds = setOf("a8")),
            MockPosting("8", "주말 브런치 서빙", CAFE, at(9, 10), at(9, 14), 12000, 0,
                PostingStatus.DONE, isUrgent = false, applicantIds = listOf("a5", "a2"), matchedWorkerId = "a5"),
        )
    }

    /** Shifts already running when the demo starts (postings 5, 6, 7). */
    fun works(postings: List<MockPosting>): List<OwnerWork> = listOf(
        seedWork(postings, "5", "a7", "박서연", isVerified = true, distance = 0),
        seedWork(postings, "6", "a9", "최지훈", isVerified = false, distance = 1400),
        seedWork(postings, "7", "a11", "윤도현", isVerified = false, distance = 2100),
    )

    fun pastPostings(postings: List<MockPosting>, workers: Map<String, MockWorker>): List<PastPosting> {
        fun past(id: String, status: PastPostingStatus, settlement: SettlementRecord?): PastPosting {
            val posting = postings.first { it.id == id }
            return PastPosting(
                postingId = id,
                workSummary = posting.workSummary,
                workerName = workers.getValue(checkNotNull(posting.matchedWorkerId)).name,
                workStartMillis = posting.startMillis,
                workEndMillis = posting.endMillis,
                hourlyWage = posting.hourlyWage,
                status = status,
                settlement = settlement,
            )
        }
        val posting3 = postings.first { it.id == "3" }
        return listOf(
            // 2h × (13,000 + 2,000) = 30,000, paid 30 minutes after the shift.
            past("3", PastPostingStatus.COMPLETED, SettlementRecord(30000, posting3.endMillis + 30 * MINUTE)),
            past("4", PastPostingStatus.NO_SHOW, settlement = null),
            // Dispute: payment on hold.
            past("8", PastPostingStatus.DISPUTE, SettlementRecord(48000, paidAtMillis = null)),
        )
    }

    private fun seedWork(
        postings: List<MockPosting>,
        postingId: String,
        workerId: String,
        workerName: String,
        isVerified: Boolean,
        distance: Int,
    ): OwnerWork {
        val posting = postings.first { it.id == postingId }
        return OwnerWork(
            id = "w$postingId",
            postingId = postingId,
            workSummary = posting.workSummary,
            workerId = workerId,
            workerName = workerName,
            scheduledStartMillis = posting.startMillis,
            scheduledEndMillis = posting.endMillis,
            hourlyWage = posting.hourlyWage,
            bonusPerHour = posting.bonusPerHour,
            isAttendanceVerified = isVerified,
            workerDistanceMeters = distance,
            status = WorkProgressStatus.ACTIVE,
        )
    }

    fun workers(): List<MockWorker> = listOf(
        worker("a1", "김민수", 24, 4.8, 8, 94, NoShowRiskLevel.LOW, setOf(RESTAURANT, CAFE), 92, 0, null),
        worker("a2", "이하은", 22, 4.5, 12, 87, NoShowRiskLevel.LOW, setOf(CAFE), 85, 0, null),
        worker("a3", "정우진", 27, 4.1, 15, 76, NoShowRiskLevel.MIDDLE, setOf(RESTAURANT), 71, 1, "2026.08.14"),
        worker("a4", "한소희", 21, 3.6, 22, 58, NoShowRiskLevel.HIGH, emptySet(), 48, 3, "2026.09.02"),
        worker("a5", "오지민", 25, 4.3, 10, 82, NoShowRiskLevel.LOW, setOf(RESTAURANT, CAFE), 80, 0, null),
        worker("a6", "이민지", 23, 4.9, 6, 96, NoShowRiskLevel.LOW, setOf(RESTAURANT), 95, 0, null),
        worker("a7", "박서연", 26, 4.7, 5, 91, NoShowRiskLevel.LOW, setOf(CAFE), 90, 0, null),
        worker("a8", "강태오", 29, 3.4, 18, 55, NoShowRiskLevel.HIGH, setOf(CAFE), 42, 4, "2026.09.26"),
        worker("a9", "최지훈", 24, 4.0, 20, 73, NoShowRiskLevel.MIDDLE, setOf(CONVENIENCE), 68, 1, "2026.07.30"),
        worker("a10", "정하늘", 22, 4.6, 9, 88, NoShowRiskLevel.LOW, setOf(CONVENIENCE, CAFE), 87, 0, null),
        worker("a11", "윤도현", 28, 3.9, 30, 64, NoShowRiskLevel.MIDDLE, emptySet(), 63, 2, "2026.08.21"),
    )

    @Suppress("LongParameterList")
    private fun worker(
        id: String,
        name: String,
        age: Int,
        rating: Double,
        etaMinutes: Int,
        matchScore: Int,
        risk: NoShowRiskLevel,
        categories: Set<String>,
        trustScore: Int,
        noShowCount: Int,
        lastNoShowDate: String?,
    ) = MockWorker(
        id = id,
        name = name,
        age = age,
        rating = rating,
        etaMinutes = etaMinutes,
        matchScore = matchScore,
        noShowRisk = risk,
        experiencedCategories = categories,
        trustScore = trustScore,
        recentWorks = recentWorksFor(id, rating),
        noShowCount = noShowCount,
        lastNoShowDate = lastNoShowDate,
    )

    private val PLACES = listOf("역삼 카페 온도", "강남 한식당 소반", "GS25 역삼점", "신논현 브런치랩", "선릉 국수집", "CU 강남대로점")

    /** Deterministic recent history (up to 5 entries, UI spec 2-3: 최근 5건). */
    private fun recentWorksFor(id: String, rating: Double): List<ApplicantWorkRecord> {
        val seed = id.drop(1).toInt()
        val count = (seed % 3) + 3
        return (0 until count).map { index ->
            ApplicantWorkRecord(
                place = PLACES[(seed + index) % PLACES.size],
                dateText = "2026.09.%02d".format(24 - index * 4 - seed % 3),
                rating = (rating.toInt() + if ((seed + index) % 2 == 0) 1 else 0).coerceIn(1, 5),
            )
        }
    }
}
