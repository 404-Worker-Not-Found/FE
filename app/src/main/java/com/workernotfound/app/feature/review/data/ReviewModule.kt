package com.workernotfound.app.feature.review.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.*
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.review.domain.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReviewRepositoryImpl @Inject constructor(private val api: ReviewApi, private val backend: BackendAccess,
    private val profiles: MemberProfileApiService, private val jobs: WorkerJobApiService) : ReviewRepository {
    override suspend fun pending(page: Int): PendingReviewPage {
        backend.requireSession()
        val response = api.pending(page).requireData()
        val contexts = response.works.map { it.jobPostId }.distinct().associateWith { jobs.detail(it).requireData() }
        return PendingReviewPage(response.works.map { target ->
            val job = contexts.getValue(target.jobPostId)
            ReviewTarget(target.workId, target.jobPostId, target.revieweeMemberId, target.workDate,
                target.completedAt, target.writableUntil, job.storeName, job.description)
        }, nextReviewPage(page, response.page, response.size, response.totalCount, response.works.size))
    }
    override suspend fun received(page: Int): ReceivedReviewPage {
        backend.requireSession()
        val member = profiles.get().requireData().memberId
        val response = api.received(member, page).requireData()
        if (response.memberId != member) throw ApiException("REVIEW_MEMBER_MISMATCH")
        return ReceivedReviewPage(response.reviews.map { it.toDomain() },
            nextReviewPage(page, response.page, response.size, response.totalCount, response.reviews.size),
            response.reviewCount, if (response.reviewCount > 0) response.averageRating else null)
    }
    override suspend fun write(target: ReviewTarget, rating: Int, comment: String) {
        backend.requireSession()
        require(rating in 1..5 && comment.length <= 500)
        val normalized = comment.trim().takeIf { it.isNotEmpty() }
        val result = api.write(WriteReviewDto(target.workId, rating, normalized)).requireData()
        confirmReview(result, target.workId, target.revieweeId, rating, normalized)
    }
}
@Module @InstallIn(SingletonComponent::class)
object ReviewModule {
    @Provides fun api(client: OkHttpClient): ReviewApi = Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
        .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(ReviewApi::class.java)
    @Provides fun repository(impl: ReviewRepositoryImpl): ReviewRepository = impl
}
