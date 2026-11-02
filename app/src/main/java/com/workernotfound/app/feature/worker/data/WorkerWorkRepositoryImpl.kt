package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerWorkApiService
import com.workernotfound.app.feature.worker.data.remote.startsInSeconds
import com.workernotfound.app.feature.worker.domain.model.WorkSchedule
import com.workernotfound.app.feature.worker.domain.model.WorkStatus
import com.workernotfound.app.feature.worker.domain.repository.WorkerWorkRepository
import javax.inject.Inject
import kotlinx.coroutines.delay

/** Mock 근무 관리 repository (UI spec 3-6: 예정 근무 / 진행 중 근무). */
class WorkerWorkRepositoryImpl @Inject constructor(private val backend: BackendAccess,
    private val api: WorkerWorkApiService,
    private val jobs: WorkerJobApiService,
) : WorkerWorkRepository {

    override suspend fun getWorks(): List<WorkSchedule> {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val all = mutableListOf<WorkSchedule>()
            var page = 0
            do {
                val result = api.list(page).requireData()
                for (work in result.content.filter { it.status in setOf("SCHEDULED", "CHECKED_IN", "IN_PROGRESS") }) {
                    val job = jobs.detail(work.jobPostId).requireData()
                    all += WorkSchedule(
                        id = work.workId.toString(), jobId = work.jobPostId.toString(), address = job.address, storeName = job.storeName, category = job.categoryName,
                        workDate = work.workDate, timeRange = "${work.startTime.take(5)} ~ ${work.endTime.take(5)}",
                        hourlyWage = job.baseHourlyWage + job.extraWage,
                        status = if (work.status == "SCHEDULED" && startsInSeconds(work.workDate, work.startTime) > 3600) WorkStatus.SCHEDULED else WorkStatus.IN_PROGRESS,
                    )
                }
                page++
            } while (page < result.totalPages)
            return all
        }
        delay(300)
        return WORKS
    }

    companion object {
        internal val WORKS = listOf(
            WorkSchedule(
                id = "w1",
                jobId = "r1",
                address = "서울 마포구 신촌로 100",
                storeName = "1번 편의점",
                category = "편의점",
                workDate = "2026.06.18 (목)",
                timeRange = "09:00 ~ 14:00",
                hourlyWage = 11800,
                status = WorkStatus.SCHEDULED,
            ),
            WorkSchedule(
                id = "w2",
                address = "서울 마포구 어울마당로 12",
                storeName = "다정있는 비치",
                category = "음식점",
                workDate = "2026.06.19 (금)",
                timeRange = "18:00 ~ 22:00",
                hourlyWage = 13500,
                status = WorkStatus.SCHEDULED,
            ),
            WorkSchedule(
                id = "w3",
                jobId = "u2",
                address = "서울 마포구 와우산로 29",
                storeName = "홀대 카페 123",
                category = "카페",
                workDate = "2026.06.17 (수)",
                timeRange = "13:00 ~ 18:00",
                hourlyWage = 12000,
                status = WorkStatus.IN_PROGRESS,
            ),
        )
    }
}
