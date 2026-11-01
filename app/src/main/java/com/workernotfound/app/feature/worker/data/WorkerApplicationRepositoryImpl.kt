package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.data.remote.ApplicationResponseDto
import com.workernotfound.app.feature.worker.data.remote.CreateApplicationRequestDto
import com.workernotfound.app.feature.worker.data.remote.MatchingResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkerApplicationApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerChatApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerApplication
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import java.util.NoSuchElementException
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * Mock application/matching repository (UI spec 3-4, 3-5). The seeded list covers
 * every status so the demo shows 지원중 / 매칭중 / 매칭완료 / 미선정(+재매칭) at once.
 */
class WorkerApplicationRepositoryImpl @Inject constructor(private val backend: BackendAccess,
    private val api: WorkerApplicationApiService,
    private val chat: WorkerChatApiService,
    private val jobs: WorkerJobApiService,
) : WorkerApplicationRepository {

    override suspend fun respondToMatching(applicationId: String, accept: Boolean): WorkerApplication {
        backend.requireSession()
        val matching = allMatchings().firstOrNull { it.applicationId.toString() == applicationId && it.status == "PENDING" }
            ?: throw ApiException("MATCHING_NOT_PENDING")
        val response = if (accept) api.accept(matching.matchingId).requireData() else api.decline(matching.matchingId).requireData()
        if (response.status != if (accept) "CONFIRMED" else "DECLINED") throw ApiException("UNEXPECTED_MATCHING_STATUS")
        return map(api.detail(applicationId.toLong()).requireData(), response, includeChat = accept)
    }

    override suspend fun apply(jobId: String) {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            api.apply(CreateApplicationRequestDto(jobId.toLong())).requireData()
        }
    }

    override suspend fun cancel(applicationId: String) {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            api.cancel(applicationId.toLong()).requireData()
        }
    }

    override suspend fun getApplications(): List<WorkerApplication> {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val matchings = allMatchings()
            val all = mutableListOf<WorkerApplication>()
            var page = 0
            do {
                val result = api.list(page).requireData()
                for (app in result.applications) all += map(app, matchings.firstOrNull { it.applicationId == app.applicationId }, includeChat = false)
                page++
            } while (page < result.totalPages)
            return all
        }
        delay(300)
        return APPLICATIONS
    }

    override suspend fun getApplication(id: String): WorkerApplication {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val app = api.detail(id.toLong()).requireData()
            return map(app, allMatchings().firstOrNull { it.applicationId == app.applicationId }, includeChat = true)
        }
        delay(200)
        return APPLICATIONS.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("지원 내역을 찾을 수 없습니다: $id")
    }

    private suspend fun allMatchings(): List<MatchingResponseDto> {
        val result = mutableListOf<MatchingResponseDto>()
        var page = 0
        do {
            val response = api.matchings(page).requireData()
            result += response.matchings
            page++
        } while (page < response.totalPages)
        return result
    }

    private suspend fun findChatRoom(matchingId: Long): String? {
        var page = 0
        do {
            val response = chat.rooms(page).requireData()
            response.content.firstOrNull { it.matchingId == matchingId }?.let { return it.chatRoomId.toString() }
            page++
        } while (page < response.totalPages)
        return null
    }

    private suspend fun map(app: ApplicationResponseDto, matching: MatchingResponseDto?, includeChat: Boolean): WorkerApplication {
        val job = jobs.detail(app.jobPostId).requireData()
        return WorkerApplication(
            id = app.applicationId.toString(), storeName = job.storeName, category = job.categoryName,
            appliedAtText = app.appliedAt, status = applicationStatus(app.status, matching?.status),
            workDate = job.workDate, workTimeRange = "${job.startTime.take(5)} ~ ${job.endTime.take(5)}",
            address = job.address, hourlyWage = job.baseHourlyWage + job.extraWage,
            matchScore = matching?.totalScore?.let { kotlin.math.round(it).toInt() },
            hasRematchOffer = matching?.status == "PENDING", matchingId = matching?.matchingId?.toString(),
            proposalExpiresInSeconds = matching?.expiresAt?.let { expiresInSeconds(it) },
            chatRoomId = if (includeChat && matching?.status in setOf("CONFIRMED", "COMPLETED")) findChatRoom(requireNotNull(matching).matchingId) else null,

        )
    }

    private companion object {
        val APPLICATIONS = listOf(
            WorkerApplication(
                id = "u1",
                storeName = "다정있는 비치",
                category = "음식점",
                appliedAtText = "오늘 14:32",
                status = ApplicationStatus.MATCHING,
                workDate = "2026.06.17 (수)",
                workTimeRange = "18:00 ~ 22:00",
                address = "서울 마포구 어울마당로 12",
                hourlyWage = 13500,
                matchScore = 87,
            ),
            WorkerApplication(
                id = "u2",
                storeName = "홀대 카페 123",
                category = "카페",
                appliedAtText = "오늘 14:05",
                status = ApplicationStatus.APPLYING,
                workDate = "2026.06.17 (수)",
                workTimeRange = "13:00 ~ 18:00",
                address = "서울 마포구 와우산로 29",
                hourlyWage = 12000,
                matchScore = 79,
            ),
            WorkerApplication(
                id = "r1",
                storeName = "1번 편의점",
                category = "편의점",
                appliedAtText = "어제 20:11",
                status = ApplicationStatus.MATCHED,
                workDate = "2026.06.18 (목)",
                workTimeRange = "09:00 ~ 14:00",
                address = "서울 마포구 신촌로 100",
                hourlyWage = 11800,
                matchScore = 92,
            ),
            WorkerApplication(
                id = "r2",
                storeName = "패스트 식당",
                category = "음식점",
                appliedAtText = "어제 18:40",
                status = ApplicationStatus.NOT_SELECTED,
                workDate = "2026.06.18 (목)",
                workTimeRange = "11:00 ~ 15:00",
                address = "서울 서대문구 연세로 8",
                hourlyWage = 12000,
                matchScore = 74,
                hasRematchOffer = true,
                etaMinutes = 12,
            ),
        )
    }
}

/** SELECTED is committed only after the confirmation saga has created work and chat. */
internal fun applicationStatus(applicationStatus: String, matchingStatus: String?): ApplicationStatus = when (applicationStatus) {
    "CANCELED" -> ApplicationStatus.CANCELED
    "REJECTED" -> ApplicationStatus.NOT_SELECTED
    "SELECTED" -> ApplicationStatus.MATCHED
    "APPLIED" -> if (matchingStatus == "PENDING") ApplicationStatus.MATCHING else ApplicationStatus.APPLYING
    else -> throw ApiException("UNKNOWN_APPLICATION_STATUS")
}

internal fun expiresInSeconds(expiresAt: String, clock: java.time.Clock = java.time.Clock.systemUTC()): Long {
    val expires = java.time.LocalDateTime.parse(expiresAt).atZone(java.time.ZoneId.of("Asia/Seoul")).toInstant()
    return java.time.Duration.between(clock.instant(), expires).seconds.coerceAtLeast(0)
}
