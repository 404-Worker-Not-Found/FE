package com.workernotfound.app.feature.worker.domain.model

/**
 * Multi-filter for job search (UI spec 3-2: 거리 / 시급 범위 / 시간대 / 업종).
 * Default values mean "no restriction" so an unset filter returns everything.
 */
data class JobFilter(
    val maxDistanceKm: Int = MAX_DISTANCE_KM,
    val minWage: Int = 0,
    val timeSlot: TimeSlot = TimeSlot.ANY,
    val categories: Set<String> = emptySet(),
    val urgentOnly: Boolean = false,
) {
    val isActive: Boolean
        get() = maxDistanceKm < MAX_DISTANCE_KM || minWage > 0 ||
            timeSlot != TimeSlot.ANY || categories.isNotEmpty() || urgentOnly

    companion object {
        const val MAX_DISTANCE_KM = 10
    }
}

/** Work time-of-day buckets for the 시간대 filter. */
enum class TimeSlot(val label: String) {
    ANY("전체"),
    MORNING("오전"),
    AFTERNOON("오후"),
    EVENING("저녁"),
}
