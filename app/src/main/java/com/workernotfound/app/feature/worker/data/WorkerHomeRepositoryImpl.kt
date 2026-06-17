package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.WorkerHomeRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock-backed worker home repository for the demo. In-memory data, with a small
 * delay to exercise the loading state. Replace with a network data source once
 * the backend contract exists; the interface and UI stay unchanged.
 */
class WorkerHomeRepositoryImpl @Inject constructor() : WorkerHomeRepository {

    override suspend fun getUrgentJobs(): List<WorkerJobPosting> {
        delay(300)
        return URGENT_JOBS
    }

    override suspend fun getRecommendedJobs(): List<WorkerJobPosting> {
        delay(300)
        return RECOMMENDED_JOBS
    }

    private companion object {
        val URGENT_JOBS = listOf(
            WorkerJobPosting(
                id = "u1",
                storeName = "다정있는 비치",
                category = "음식점",
                workTime = "18:00 ~ 22:00",
                hourlyWage = 13500,
                distanceText = "0.5km",
                isUrgent = true,
                deadlineText = "마감 12분 전",
            ),
            WorkerJobPosting(
                id = "u2",
                storeName = "홀대 카페 123",
                category = "카페",
                workTime = "13:00 ~ 18:00",
                hourlyWage = 12000,
                distanceText = "0.8km",
                isUrgent = true,
                deadlineText = "마감 31분 전",
            ),
        )

        val RECOMMENDED_JOBS = listOf(
            WorkerJobPosting(
                id = "r1",
                storeName = "1번 편의점",
                category = "편의점",
                workTime = "09:00 ~ 14:00",
                hourlyWage = 11800,
                distanceText = "1.2km",
                isUrgent = false,
            ),
            WorkerJobPosting(
                id = "r2",
                storeName = "패스트 식당",
                category = "음식점",
                workTime = "11:00 ~ 15:00",
                hourlyWage = 12000,
                distanceText = "1.8km",
                isUrgent = false,
            ),
            WorkerJobPosting(
                id = "r3",
                storeName = "인디룸 카페",
                category = "카페",
                workTime = "15:00 ~ 20:00",
                hourlyWage = 11000,
                distanceText = "2.4km",
                isUrgent = false,
            ),
        )
    }
}
