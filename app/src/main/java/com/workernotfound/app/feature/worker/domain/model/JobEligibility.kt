package com.workernotfound.app.feature.worker.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Client-side rules over real server fields. The job search contract has no per-job
 * 모집 반경 or recommendation API, so the worker's own 활동 반경 and signup preferences are used.
 */
object JobEligibility {

    /** 즉시 지원 가능: recruiting, before the application deadline, not applied yet, inside the worker's radius. */
    fun isApplicable(job: WorkerJobPosting, preference: WorkerPreference?): Boolean =
        job.status == STATUS_OPEN && job.remainingMinutes > 0 &&
            job.applyStatus == ApplyStatus.AVAILABLE && withinRadius(job, preference) == true

    /** null when either the worker's radius or the job distance is unknown. */
    fun withinRadius(job: WorkerJobPosting, preference: WorkerPreference?): Boolean? {
        val radius = preference?.activityRadiusKm ?: return null
        val distance = job.distanceKm ?: return null
        return distance <= radius
    }

    /** 추천 공고: applicable and matching the worker's preferred business type, wage and available time. */
    fun isRecommended(job: WorkerJobPosting, preference: WorkerPreference?): Boolean {
        if (preference == null || !isApplicable(job, preference)) return false
        return matchesBusinessType(job, preference) && matchesWage(job, preference) && matchesTime(job, preference)
    }

    private fun matchesBusinessType(job: WorkerJobPosting, preference: WorkerPreference): Boolean =
        preference.preferredBusinessTypes.isEmpty() ||
            preference.preferredBusinessTypes.any { it.trim().equals(job.category.trim(), ignoreCase = true) }

    private fun matchesWage(job: WorkerJobPosting, preference: WorkerPreference): Boolean =
        preference.desiredHourlyWage == null || job.hourlyWage >= preference.desiredHourlyWage

    private fun matchesTime(job: WorkerJobPosting, preference: WorkerPreference): Boolean {
        if (preference.availableTimes.isEmpty()) return true
        val date = runCatching { LocalDate.parse(job.workDate) }.getOrNull() ?: return false
        val start = parseTime(job.startTime) ?: return false
        val end = parseTime(job.endTime) ?: return false
        val overnight = !end.isAfter(start)
        return preference.availableTimes.any { slot ->
            slot.day == date.dayOfWeek && !start.isBefore(slot.start) &&
                (if (overnight) !slot.end.isAfter(slot.start) else !end.isAfter(slot.end) || !slot.end.isAfter(slot.start))
        }
    }

    private fun parseTime(raw: String): LocalTime? = runCatching { LocalTime.parse(raw) }.getOrNull()

    const val STATUS_OPEN = "OPEN"
}
