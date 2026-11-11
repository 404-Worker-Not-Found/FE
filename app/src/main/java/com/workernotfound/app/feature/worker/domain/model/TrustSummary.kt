package com.workernotfound.app.feature.worker.domain.model

import java.math.BigDecimal

/**
 * 나의 신뢰도 화면 데이터 (UI spec 4-2).
 *
 * Counts, reviews and history come from the worker's own works, received reviews and settlements.
 * [score], [grade] and [noShowRisk] stay null: matching-service keeps the trust score internally and
 * exposes no public endpoint, so the app never invents them.
 */
data class TrustSummary(
    val score: Int?,
    val grade: String?,
    val noShowRisk: NoShowRiskLevel?,
    val totalWorkCount: Int,      // 완료한 근무
    val noShowCount: Int,         // 노쇼로 끝난 근무
    val finishedCount: Int,       // 종료된 근무 전체 (완료·노쇼·취소·실패)
    val averageRating: Double?,
    val reviewCount: Long,
    val recentReviews: List<Review>,
    val workHistory: List<WorkHistoryItem>,
)

/** 노쇼 위험도 (UI spec 4-2: LOW / MIDDLE / HIGH). */
enum class NoShowRiskLevel { LOW, MIDDLE, HIGH }

/** 최근 받은 리뷰 1건. [storeName] is null when the review's work is not in the worker's history. */
data class Review(
    val id: String,
    val storeName: String?,
    val rating: Int,
    val comment: String?,
    val dateText: String,
)

/** 최근 근무 이력 1건: 가게명·날짜·그 근무에서 받은 평점·실제 정산 금액. */
data class WorkHistoryItem(
    val id: String,
    val storeName: String,
    val dateText: String,
    val rating: Int?,
    val settlementAmount: BigDecimal?,
    val settlementCurrency: String?,
)
