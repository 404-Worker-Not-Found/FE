package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.TimeSlot
import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.JobSearchResult
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import kotlinx.coroutines.delay
import java.util.NoSuchElementException
import javax.inject.Inject

/**
 * Mock-backed job search/detail repository. A single in-memory list of
 * [WorkerJobDetail] is the source of truth; search maps it to card models and
 * applies the [JobFilter]. The ids include the ones the home screen links to
 * (u1, u2, r1...) so navigation from home resolves to a detail here.
 */
class WorkerJobRepositoryImpl @Inject constructor() : WorkerJobRepository {

    override suspend fun searchJobs(filter: JobFilter): JobSearchResult {
        delay(300)
        val matched = JOBS.filter { it.matches(filter) }
        val (urgent, recommended) = matched.partition { it.isUrgent }
        return JobSearchResult(
            urgent = urgent.map { it.toCard() },
            recommended = recommended.map { it.toCard() },
        )
    }

    override suspend fun getJobDetail(id: String): WorkerJobDetail {
        delay(200)
        return JOBS.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("공고를 찾을 수 없습니다: $id")
    }

    private fun WorkerJobDetail.matches(filter: JobFilter): Boolean {
        if (filter.urgentOnly && !isUrgent) return false
        if (distanceKm > filter.maxDistanceKm) return false
        if (finalWage < filter.minWage) return false
        if (filter.timeSlot != TimeSlot.ANY && timeSlotOf() != filter.timeSlot) return false
        if (filter.categories.isNotEmpty() && category !in filter.categories) return false
        return true
    }

    /** Bucket the opening by its start hour for the 시간대 filter. */
    private fun WorkerJobDetail.timeSlotOf(): TimeSlot {
        val startHour = workTimeRange.substringBefore(":").trim().toIntOrNull() ?: return TimeSlot.ANY
        return when {
            startHour < 12 -> TimeSlot.MORNING
            startHour < 17 -> TimeSlot.AFTERNOON
            else -> TimeSlot.EVENING
        }
    }

    private fun WorkerJobDetail.toCard() = WorkerJobPosting(
        id = id,
        storeName = storeName,
        category = category,
        workTime = workTimeRange,
        hourlyWage = finalWage,
        distanceText = formatDistance(distanceKm),
        isUrgent = isUrgent,
        deadlineText = applyDeadlineText,
    )

    private companion object {
        fun formatDistance(km: Double): String =
            if (km < 1.0) "${(km * 1000).toInt()}m" else "%.1fkm".format(km)

        val JOBS = listOf(
            WorkerJobDetail(
                id = "u1",
                storeName = "다정있는 비치",
                category = "음식점",
                address = "서울 마포구 어울마당로 12",
                distanceKm = 0.5,
                baseWage = 11000,
                urgentBonus = 1500,
                timeBonus = 1000,
                workDate = "2026.06.17 (수)",
                workTimeRange = "18:00 ~ 22:00",
                totalWorkHours = "4시간",
                startsInSeconds = 3 * 3600 + 12 * 60,
                applyDeadlineText = "마감 12분 전",
                description = "홀 서빙 및 간단한 마감 정리. 경험 없어도 친절히 알려드려요.",
                withinRadius = true,
                radiusText = "모집 반경 1km 이내",
                applicantCount = 3,
                matchScore = 87,
                isUrgent = true,
            ),
            WorkerJobDetail(
                id = "u2",
                storeName = "홀대 카페 123",
                category = "카페",
                address = "서울 마포구 와우산로 29",
                distanceKm = 0.8,
                baseWage = 10500,
                urgentBonus = 1500,
                timeBonus = 0,
                workDate = "2026.06.17 (수)",
                workTimeRange = "13:00 ~ 18:00",
                totalWorkHours = "5시간",
                startsInSeconds = 31 * 60,
                applyDeadlineText = "마감 31분 전",
                description = "음료 제조 보조 및 매장 청결 관리.",
                withinRadius = true,
                radiusText = "모집 반경 1km 이내",
                applicantCount = 5,
                matchScore = 79,
                isUrgent = true,
            ),
            WorkerJobDetail(
                id = "r1",
                storeName = "1번 편의점",
                category = "편의점",
                address = "서울 마포구 신촌로 100",
                distanceKm = 1.2,
                baseWage = 11800,
                urgentBonus = 0,
                timeBonus = 0,
                workDate = "2026.06.18 (목)",
                workTimeRange = "09:00 ~ 14:00",
                totalWorkHours = "5시간",
                startsInSeconds = 18 * 3600,
                description = "상품 진열, 계산, 매장 정리. 야간 아님.",
                withinRadius = true,
                radiusText = "모집 반경 3km 이내",
                applicantCount = 1,
                matchScore = 92,
                isUrgent = false,
            ),
            WorkerJobDetail(
                id = "r2",
                storeName = "패스트 식당",
                category = "음식점",
                address = "서울 서대문구 연세로 8",
                distanceKm = 1.8,
                baseWage = 12000,
                urgentBonus = 0,
                timeBonus = 0,
                workDate = "2026.06.18 (목)",
                workTimeRange = "11:00 ~ 15:00",
                totalWorkHours = "4시간",
                startsInSeconds = 20 * 3600,
                description = "주방 보조 및 홀 서빙. 점심 피크타임.",
                withinRadius = false,
                radiusText = "모집 반경 밖 (1.8km)",
                applicantCount = 2,
                matchScore = 74,
                isUrgent = false,
            ),
            WorkerJobDetail(
                id = "r3",
                storeName = "인디룸 카페",
                category = "카페",
                address = "서울 마포구 양화로 45",
                distanceKm = 2.4,
                baseWage = 11000,
                urgentBonus = 0,
                timeBonus = 0,
                workDate = "2026.06.19 (금)",
                workTimeRange = "15:00 ~ 20:00",
                totalWorkHours = "5시간",
                startsInSeconds = 44 * 3600,
                description = "디저트 플레이팅 보조 및 홀 관리.",
                withinRadius = false,
                radiusText = "모집 반경 밖 (2.4km)",
                applicantCount = 0,
                matchScore = 68,
                isUrgent = false,
            ),
        )
    }
}
