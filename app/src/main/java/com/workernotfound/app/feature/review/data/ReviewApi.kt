package com.workernotfound.app.feature.review.data

import com.workernotfound.app.core.network.ApiResponse
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.review.domain.ReceivedReview
import java.math.BigDecimal
import retrofit2.http.*

interface ReviewApi {
    @GET("api/members/me/reviews/pending") suspend fun pending(@Query("page") page: Int, @Query("size") size: Int = 20): ApiResponse<PendingPageDto>
    @GET("api/members/{id}/reviews") suspend fun received(@Path("id") id: Long, @Query("page") page: Int, @Query("size") size: Int = 20): ApiResponse<ReceivedPageDto>
    @POST("api/members/me/reviews") suspend fun write(@Body body: WriteReviewDto): ApiResponse<WrittenReviewDto>
}
data class PendingTargetDto(val workId: Long, val jobPostId: Long, val revieweeMemberId: Long,
    val workDate: String, val completedAt: String, val writableUntil: String)
data class PendingPageDto(val works: List<PendingTargetDto>, val page: Int, val size: Int, val totalCount: Long)
data class ReceivedReviewDto(val reviewId: Long, val reviewerRole: String, val rating: Int, val comment: String?, val createdAt: String,
    val workId: Long? = null) {
    fun toDomain() = ReceivedReview(reviewId, reviewerRole, rating, comment, createdAt, workId)
}
data class ReceivedPageDto(val memberId: Long, val reviewCount: Long, val averageRating: BigDecimal?,
    val reviews: List<ReceivedReviewDto>, val page: Int, val size: Int, val totalCount: Long)
class WriteReviewDto(val workId: Long, val rating: Int, val comment: String?)
data class WrittenReviewDto(val reviewId: Long, val workId: Long, val revieweeMemberId: Long, val rating: Int, val comment: String?)

internal fun nextReviewPage(requested: Int, page: Int, size: Int, count: Long, returned: Int): Int? {
    if (requested != page || size <= 0 || count < 0) throw ApiException("INVALID_REVIEW_PAGE")
    val more = (page.toLong() + 1) * size < count
    if (more && returned == 0) throw ApiException("EMPTY_REVIEW_PAGE")
    return if (more) page + 1 else null
}
internal fun confirmReview(result: WrittenReviewDto, workId: Long, revieweeId: Long, rating: Int, comment: String?) {
    if (result.reviewId <= 0 || result.workId != workId || result.revieweeMemberId != revieweeId ||
        result.rating != rating || result.comment != comment) throw ApiException("REVIEW_NOT_CONFIRMED")
}
