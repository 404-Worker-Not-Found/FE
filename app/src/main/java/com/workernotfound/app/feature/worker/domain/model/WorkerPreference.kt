package com.workernotfound.app.feature.worker.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/** Worker signup preferences (member-service workerProfile). Drives 추천 공고 and 즉시 지원 가능. */
data class WorkerPreference(
    val name: String,
    val desiredHourlyWage: Int?,
    val activityRadiusKm: Int?,
    val baseLatitude: Double?,
    val baseLongitude: Double?,
    val preferredBusinessTypes: List<String>,
    val availableTimes: List<AvailableTime>,
) {
    val hasBaseLocation: Boolean
        get() = baseLatitude != null && baseLongitude != null &&
            baseLatitude.isFinite() && baseLongitude.isFinite() &&
            baseLatitude in -90.0..90.0 && baseLongitude in -180.0..180.0
}

data class AvailableTime(val day: DayOfWeek, val start: LocalTime, val end: LocalTime)
