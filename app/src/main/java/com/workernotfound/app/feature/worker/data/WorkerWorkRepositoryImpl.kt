package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.WorkSchedule
import com.workernotfound.app.feature.worker.domain.model.WorkStatus
import com.workernotfound.app.feature.worker.domain.repository.WorkerWorkRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Mock 근무 관리 repository (UI spec 3-6: 예정 근무 / 진행 중 근무). */
class WorkerWorkRepositoryImpl @Inject constructor() : WorkerWorkRepository {

    override suspend fun getWorks(): List<WorkSchedule> {
        delay(300)
        return WORKS
    }

    private companion object {
        val WORKS = listOf(
            WorkSchedule(
                id = "w1",
                storeName = "1번 편의점",
                category = "편의점",
                workDate = "2026.06.18 (목)",
                timeRange = "09:00 ~ 14:00",
                hourlyWage = 11800,
                status = WorkStatus.SCHEDULED,
            ),
            WorkSchedule(
                id = "w2",
                storeName = "다정있는 비치",
                category = "음식점",
                workDate = "2026.06.19 (금)",
                timeRange = "18:00 ~ 22:00",
                hourlyWage = 13500,
                status = WorkStatus.SCHEDULED,
            ),
            WorkSchedule(
                id = "w3",
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
