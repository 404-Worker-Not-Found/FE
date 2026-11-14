package com.workernotfound.app.feature.worker.domain.model

/**
 * A job opening shown on the worker home (UI spec 3-1: 긴급 공고 / 추천 공고).
 *
 * The home shows two groups of the same shape: urgent openings (with a remaining
 * time) and recommended openings. [hourlyWage] is the displayed wage (base + the
 * system-added surcharge combined).
 */
data class WorkerJobPosting(
    val id: String,
    val storeName: String,
    val category: String,
    val workTime: String,
    val hourlyWage: Int,
    val distanceText: String,
    val isUrgent: Boolean,
    val deadlineText: String? = null,
    val applyStatus: ApplyStatus = ApplyStatus.AVAILABLE,
    /** Raw server values used for eligibility/recommendation rules. */
    val distanceKm: Double? = null,
    val remainingMinutes: Long = 0,
    val status: String = JobEligibility.STATUS_OPEN,
    val workDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
)

/** Instant-apply state for a posting (UI spec 3-1: 성공 시 지원 완료 상태 표시). */
enum class ApplyStatus { AVAILABLE, APPLIED, REAPPLICATION_NOT_ALLOWED }
