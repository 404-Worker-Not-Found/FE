package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerApplication
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import kotlinx.coroutines.delay
import java.util.NoSuchElementException
import javax.inject.Inject

/**
 * Mock application/matching repository (UI spec 3-4, 3-5). The seeded list covers
 * every status so the demo shows 지원중 / 매칭중 / 매칭완료 / 미선정(+재매칭) at once.
 */
class WorkerApplicationRepositoryImpl @Inject constructor() : WorkerApplicationRepository {

    override suspend fun getApplications(): List<WorkerApplication> {
        delay(300)
        return APPLICATIONS
    }

    override suspend fun getApplication(id: String): WorkerApplication {
        delay(200)
        return APPLICATIONS.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("지원 내역을 찾을 수 없습니다: $id")
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
