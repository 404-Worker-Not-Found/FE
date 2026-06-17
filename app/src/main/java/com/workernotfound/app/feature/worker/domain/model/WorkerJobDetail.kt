package com.workernotfound.app.feature.worker.domain.model

/**
 * Full job detail shown on the worker job-detail screen (UI spec 3-3).
 *
 * Wage is kept as its parts so the screen can show the breakdown
 * (기본 시급 + 긴급/시간대 가산); [finalWage] is the sum.
 */
data class WorkerJobDetail(
    val id: String,
    val storeName: String,
    val category: String,
    val address: String,
    val distanceKm: Double,
    val baseWage: Int,
    val urgentBonus: Int,
    val timeBonus: Int,
    val workDate: String,
    val workTimeRange: String,
    val totalWorkHours: String,
    val startsInSeconds: Long,
    val applyDeadlineText: String? = null,
    val description: String,
    val withinRadius: Boolean,
    val radiusText: String,
    val applicantCount: Int,
    val matchScore: Int,
    val isUrgent: Boolean,
    val applyStatus: ApplyStatus = ApplyStatus.AVAILABLE,
) {
    val finalWage: Int get() = baseWage + urgentBonus + timeBonus
}
