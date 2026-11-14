package com.workernotfound.app.feature.worker.domain.model

/**
 * Full job detail shown on the worker job-detail screen (UI spec 3-3).
 *
 * The detail API returns only the total surcharge ([extraWage]); urgency, distance and the
 * deadline come from the search card the worker opened ([JobEligibility] rules).
 */
data class WorkerJobDetail(
    val id: String,
    val storeName: String,
    val category: String,
    val address: String,
    val distanceKm: Double?,
    val baseWage: Int,
    val workDate: String,
    val workTimeRange: String,
    val totalWorkHours: String,
    val startsInSeconds: Long,
    val description: String,
    val applicantCount: Int?,
    val matchScore: Int?,
    val isUrgent: Boolean,
    val applyStatus: ApplyStatus = ApplyStatus.AVAILABLE,
    val applicationId: String? = null,
    val extraWage: Int = 0,
    val activityRadiusKm: Int? = null,
    val availability: ApplyAvailability = ApplyAvailability.UNKNOWN,
) {
    val finalWage: Int get() = baseWage + extraWage
}

/** 지원 가능 여부 판정 결과 (UI spec 3-3: 모집 반경 내 위치 여부). */
enum class ApplyAvailability { AVAILABLE, OUT_OF_RADIUS, CLOSED, UNKNOWN }

fun JobEligibility.availability(card: WorkerJobPosting?, preference: WorkerPreference?): ApplyAvailability = when {
    card == null -> ApplyAvailability.UNKNOWN
    card.status != JobEligibility.STATUS_OPEN || card.remainingMinutes <= 0 -> ApplyAvailability.CLOSED
    else -> when (withinRadius(card, preference)) {
        true -> ApplyAvailability.AVAILABLE
        false -> ApplyAvailability.OUT_OF_RADIUS
        null -> ApplyAvailability.UNKNOWN
    }
}
