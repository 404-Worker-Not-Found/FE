package com.workernotfound.app.feature.job.domain.model

/** 근무 날짜 — 당일 또는 익일 제한 (UI spec 2-2). */
enum class WorkDay(val label: String) {
    TODAY("오늘"),
    TOMORROW("내일"),
}

/** 최저시급 검증 기준 (UI spec 2-2: 최저시급 10,320원 이상). */
const val MIN_HOURLY_WAGE = 10_320
