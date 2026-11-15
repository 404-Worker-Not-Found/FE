package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.core.ui.KoreanDateTime
import com.workernotfound.app.feature.history.domain.WorkHistoryEntry
import com.workernotfound.app.feature.history.domain.WorkHistoryRepository
import com.workernotfound.app.feature.review.domain.ReceivedReview
import com.workernotfound.app.feature.review.domain.ReviewRepository
import com.workernotfound.app.feature.review.data.ReviewApi
import com.workernotfound.app.feature.review.data.TrustScoreDto
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.worker.domain.model.Review
import com.workernotfound.app.feature.worker.domain.model.TrustSummary
import com.workernotfound.app.feature.worker.domain.model.WorkHistoryItem
import com.workernotfound.app.feature.worker.domain.repository.WorkerTrustRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException

/** 신뢰도 (UI spec 4-2) from the worker's own works, settlements and received reviews. */
class WorkerTrustRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val history: WorkHistoryRepository,
    private val reviews: ReviewRepository,
    private val profiles: MemberProfileApiService,
    private val reviewApi: ReviewApi,
) : WorkerTrustRepository {
    override suspend fun getTrustSummary(): TrustSummary = coroutineScope {
        backend.requireSession()
        val memberId = profiles.get().requireData().memberId
        val works = async { history.getAll() }
        val received = async { reviews.received(0) }
        val score = async {
            try { reviewApi.trustScore(memberId).requireData() }
            catch (error: HttpException) {
                if (error.code() == 404) null else throw error
            }
        }
        val page = received.await()
        val trustScore = score.await()
        if (trustScore != null && trustScore.memberId != memberId) throw ApiException("TRUST_SCORE_MEMBER_MISMATCH")
        buildTrustSummary(works.await(), page.items, page.average?.toDouble(), page.count, trustScore = trustScore)
    }
}

internal fun buildTrustSummary(
    works: List<WorkHistoryEntry>,
    received: List<ReceivedReview>,
    averageRating: Double?,
    reviewCount: Long,
    recentLimit: Int = 3,
    historyLimit: Int = 5,
    trustScore: TrustScoreDto? = null,
): TrustSummary {
    val byWork = works.associateBy { it.workId }
    val ratingByWork = received.filter { it.workId != null }.associateBy({ requireNotNull(it.workId) }, { it.rating })
    val newestFirst = works.sortedWith(compareByDescending<WorkHistoryEntry> { it.date }.thenByDescending { it.startTime })
    return TrustSummary(
        score = trustScore?.trustScore,
        grade = null,
        noShowRisk = null,
        totalWorkCount = works.count { it.status == "COMPLETED" },
        noShowCount = trustScore?.noShowCount?.let(Math::toIntExact) ?: works.count { it.status == "NO_SHOW" },
        finishedCount = works.size,
        averageRating = averageRating ?: received.takeIf { it.isNotEmpty() }?.map { it.rating }?.average(),
        reviewCount = trustScore?.reviewCount ?: reviewCount,
        recentReviews = received.sortedByDescending { it.createdAt }.take(recentLimit).map { review ->
            Review(
                id = review.id.toString(),
                storeName = review.workId?.let { byWork[it]?.storeName },
                rating = review.rating,
                comment = review.comment?.takeIf { it.isNotBlank() },
                dateText = KoreanDateTime.workDate(review.createdAt),
            )
        },
        workHistory = newestFirst.take(historyLimit).map { work ->
            WorkHistoryItem(
                id = work.workId.toString(),
                storeName = work.storeName,
                dateText = KoreanDateTime.workDate(work.date),
                rating = ratingByWork[work.workId],
                settlementAmount = work.settlement?.amount,
                settlementCurrency = work.settlement?.currency,
            )
        },
    )
}
