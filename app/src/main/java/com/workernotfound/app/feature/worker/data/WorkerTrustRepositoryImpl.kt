package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.NoShowRiskLevel
import com.workernotfound.app.feature.worker.domain.model.Review
import com.workernotfound.app.feature.worker.domain.model.TrustSummary
import com.workernotfound.app.feature.worker.domain.model.WorkHistoryItem
import com.workernotfound.app.feature.worker.domain.repository.WorkerTrustRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Mock 신뢰도 repository (UI spec 4-2). */
class WorkerTrustRepositoryImpl @Inject constructor() : WorkerTrustRepository {

    override suspend fun getTrustSummary(): TrustSummary {
        delay(300)
        return TrustSummary(
            score = 92,
            grade = "우수",
            noShowRisk = NoShowRiskLevel.LOW,
            totalWorkCount = 54,
            noShowCount = 0,
            averageRating = 4.8,
            recentReviews = listOf(
                Review("rv1", "1번 편의점", 5, "시간 잘 지키고 성실해요. 또 함께하고 싶어요!", "2026.06.10"),
                Review("rv2", "다정있는 비치", 4, "친절하고 손이 빨라요.", "2026.06.03"),
                Review("rv3", "홀대 카페 123", 5, "책임감 있게 마감까지 도와줬어요.", "2026.05.28"),
            ),
            workHistory = listOf(
                WorkHistoryItem("h1", "1번 편의점", "2026.06.10", 11800, 5, 59000),
                WorkHistoryItem("h2", "다정있는 비치", "2026.06.03", 13500, 4, 54000),
                WorkHistoryItem("h3", "홀대 카페 123", "2026.05.28", 12000, 5, 60000),
            ),
        )
    }
}
