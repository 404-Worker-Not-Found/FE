package com.workernotfound.app.feature.worker.domain.model

/** 나의 신뢰도 화면 데이터 (UI spec 4-2 / 산출물 Figma). */
data class TrustSummary(
    val score: Int,              // 0~100 신뢰 점수
    val grade: String,           // 등급 (예: 우수)
    val noShowRisk: NoShowRiskLevel,
    val totalWorkCount: Int,     // 총 근무 횟수
    val noShowCount: Int,        // 노쇼 횟수
    val averageRating: Double,   // 리뷰 평균 별점
    val recentReviews: List<Review>,
    val workHistory: List<WorkHistoryItem>,
)

/** 노쇼 위험도 (UI spec 4-2: LOW / MIDDLE / HIGH). */
enum class NoShowRiskLevel { LOW, MIDDLE, HIGH }

/** 최근 리뷰 1건 (UI spec 4-2: 리뷰 점수). */
data class Review(
    val id: String,
    val storeName: String,
    val rating: Int,
    val comment: String,
    val dateText: String,
)

/** 과거 근무 이력 1건 (UI spec 4-2: 근무 이력 조회). */
data class WorkHistoryItem(
    val id: String,
    val storeName: String,
    val dateText: String,
    val hourlyWage: Int,
    val rating: Int,
    val settlement: Int,
)
