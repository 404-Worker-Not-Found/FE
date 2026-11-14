package com.workernotfound.app.feature.worker.domain.model

/**
 * Client-side rule for the separate 즉시 지원 가능 toggle. The public job list itself is unfiltered.
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

    const val STATUS_OPEN = "OPEN"
}
