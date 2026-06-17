package com.workernotfound.app.feature.job.domain.model

/** Owner job-posting form data passed to the data layer (UI spec 2-2). */
data class JobPostingDraft(
    val workDay: WorkDay,
    val startTime: String,
    val endTime: String,
    val description: String,
    val baseWage: Int,
    val recruitCount: Int,
    val radius: RecruitRadius,
    val category: JobCategory,
    val address: String,
)

/** 근무 날짜 — 당일 또는 익일 제한 (UI spec 2-2). */
enum class WorkDay(val label: String) {
    TODAY("오늘"),
    TOMORROW("내일"),
}

/** 모집 반경 — 500m / 1km / 3km / 5km (UI spec 2-2). */
enum class RecruitRadius(val label: String, val meters: Int) {
    M500("500m", 500),
    KM1("1km", 1_000),
    KM3("3km", 3_000),
    KM5("5km", 5_000),
}

/** 업종 카테고리 (UI spec 2-2). */
enum class JobCategory(val label: String) {
    RESTAURANT("음식점"),
    CAFE("카페"),
    CONVENIENCE("편의점"),
    MART("마트"),
    ETC("기타"),
}

/** 최저시급 검증 기준 (UI spec 2-2: 최저시급 10,320원 이상). */
const val MIN_HOURLY_WAGE = 10_320
