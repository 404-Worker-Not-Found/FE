package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.worker.domain.model.WorkAttendance
import com.workernotfound.app.feature.worker.domain.repository.CheckInReceipt
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerWorkApiService
import com.workernotfound.app.feature.worker.data.remote.startsInSeconds
import com.workernotfound.app.feature.worker.domain.model.WorkSchedule
import com.workernotfound.app.feature.worker.domain.model.WorkStatus
import com.workernotfound.app.feature.worker.domain.repository.WorkerWorkRepository
import javax.inject.Inject

/** 근무 관리 repository (UI spec 3-6: 예정 근무 / 진행 중 근무). */
class WorkerWorkRepositoryImpl @Inject constructor(private val backend: BackendAccess,
    private val api: WorkerWorkApiService,
    private val jobs: WorkerJobApiService,
) : WorkerWorkRepository {

    override suspend fun checkIn(workId: String, latitude: Double, longitude: Double): CheckInReceipt {
        backend.requireSession()
        val response = api.checkIn(workId.toLong(), com.workernotfound.app.feature.worker.data.remote.WorkCheckInRequestDto(latitude, longitude)).requireData()
        return confirmedCheckInReceipt(workId, response)
    }

    override suspend fun getWorks(): List<WorkSchedule> {
        backend.requireSession()
        val all = mutableListOf<WorkSchedule>()
        val policy = api.policy().requireData()
        val radius = policy.radiusMeters ?: throw ApiException("ATTENDANCE_POLICY_MISSING")
        val early = policy.earlyWindowSeconds ?: throw ApiException("ATTENDANCE_POLICY_MISSING")
        val late = policy.lateWindowSeconds ?: throw ApiException("ATTENDANCE_POLICY_MISSING")
        if (!radius.isFinite() || radius <= 0 || early < 0 || late < 0) throw ApiException("ATTENDANCE_POLICY_INVALID")
        var page = 0
        do {
            val result = api.list(page).requireData()
            for (work in result.content.filter { it.status in setOf("SCHEDULED", "CHECKED_IN", "IN_PROGRESS") }) {
                val job = jobs.detail(work.jobPostId).requireData()
                val startsAt = LocalDate.parse(work.workDate).atTime(LocalTime.parse(work.startTime))
                    .atZone(ZoneId.of(policy.timeZone)).toInstant().toEpochMilli()
                all += WorkSchedule(
                    id = work.workId.toString(), jobId = work.jobPostId.toString(), address = job.address, storeName = job.storeName, category = job.categoryName,
                    workDate = work.workDate, timeRange = "${work.startTime.take(5)} ~ ${work.endTime.take(5)}",
                    hourlyWage = job.baseHourlyWage + job.extraWage,
                    // Stays 예정 until the server confirms check-in/start, even when the start is near.
                    status = workSection(work.status),
                    attendance = WorkAttendance(work.status, work.latitude, work.longitude, radius,
                        startsAt - early * 1000, startsAt + late * 1000, work.checkedInAt != null, policy.timeZone),
                )
            }
            page++
        } while (page < result.totalPages)
        return all
    }
}


internal fun confirmedCheckInReceipt(workId: String, response: com.workernotfound.app.feature.worker.data.remote.WorkResponseDto): CheckInReceipt {
    if (response.workId.toString() != workId || response.checkedInAt.isNullOrBlank()) throw ApiException("CHECK_IN_NOT_CONFIRMED")
    return CheckInReceipt(workId, response.status, response.checkedInAt)
}

/** 예정 vs 진행 중 follows the server work status only (SCHEDULED → 예정; CHECKED_IN/IN_PROGRESS → 진행 중). */
internal fun workSection(serverStatus: String): WorkStatus =
    if (serverStatus == "SCHEDULED") WorkStatus.SCHEDULED else WorkStatus.IN_PROGRESS
