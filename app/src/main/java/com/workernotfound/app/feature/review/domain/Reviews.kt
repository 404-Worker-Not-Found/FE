package com.workernotfound.app.feature.review.domain

import java.math.BigDecimal

data class ReviewTarget(val workId: Long, val jobId: Long, val revieweeId: Long, val date: String,
    val completedAt: String, val writableUntil: String, val storeName: String, val description: String)
data class ReceivedReview(val id: Long, val role: String, val rating: Int, val comment: String?, val createdAt: String)
data class PendingReviewPage(val items: List<ReviewTarget>, val next: Int?)
data class ReceivedReviewPage(val items: List<ReceivedReview>, val next: Int?, val count: Long, val average: BigDecimal?)
interface ReviewRepository {
    suspend fun pending(page: Int = 0): PendingReviewPage
    suspend fun received(page: Int = 0): ReceivedReviewPage
    suspend fun write(target: ReviewTarget, rating: Int, comment: String)
}
