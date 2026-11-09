package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.core.ui.KoreanDateTime
import com.workernotfound.app.feature.worker.data.remote.ApplicationResponseDto
import com.workernotfound.app.feature.worker.data.remote.CreateApplicationRequestDto
import com.workernotfound.app.feature.worker.data.remote.MatchingResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkerApplicationApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerChatApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerApplication
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import javax.inject.Inject

/** Application/matching repository (UI spec 3-4, 3-5). */
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
        backend.requireSession()
        api.apply(CreateApplicationRequestDto(jobId.toLong())).requireData()
    }

    override suspend fun cancel(applicationId: String) {
        backend.requireSession()
        api.cancel(applicationId.toLong()).requireData()
    }

    override suspend fun getApplications(): List<WorkerApplication> {
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

    override suspend fun getApplication(id: String): WorkerApplication {
        backend.requireSession()
        val app = api.detail(id.toLong()).requireData()
        return map(app, allMatchings().firstOrNull { it.applicationId == app.applicationId }, includeChat = true)
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
        val expiresIn = matching?.expiresAt?.let { expiresInSeconds(it) }
        return WorkerApplication(
            id = app.applicationId.toString(), storeName = job.storeName, category = job.categoryName,
            appliedAtText = KoreanDateTime.relative(app.appliedAt), status = applicationStatus(app.status, matching?.status),
            workDate = KoreanDateTime.workDate(job.workDate), workTimeRange = "${job.startTime.take(5)} ~ ${job.endTime.take(5)}",
            address = job.address, hourlyWage = job.baseHourlyWage + job.extraWage,
            matchScore = matching?.totalScore?.let { kotlin.math.round(it).toInt() },
            hasRematchOffer = isValidOffer(app.status, matching?.status, expiresIn), matchingId = matching?.matchingId?.toString(),
            proposalExpiresInSeconds = expiresIn,
            chatRoomId = if (includeChat && matching?.status in setOf("CONFIRMED", "COMPLETED")) findChatRoom(requireNotNull(matching).matchingId) else null,

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

/** A matching proposal the worker can still answer: pending, on a live application, before expiresAt. */
internal fun isValidOffer(applicationStatus: String, matchingStatus: String?, expiresInSeconds: Long?): Boolean =
    applicationStatus == "APPLIED" && matchingStatus == "PENDING" && (expiresInSeconds ?: 0) > 0

internal fun expiresInSeconds(expiresAt: String, clock: java.time.Clock = java.time.Clock.systemUTC()): Long {
    val expires = java.time.LocalDateTime.parse(expiresAt).atZone(java.time.ZoneId.of("Asia/Seoul")).toInstant()
    return java.time.Duration.between(clock.instant(), expires).seconds.coerceAtLeast(0)
}
