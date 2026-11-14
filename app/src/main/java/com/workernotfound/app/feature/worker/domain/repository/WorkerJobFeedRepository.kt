package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.JobEligibility
import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.model.WorkerPreference

/** Job list shared by 홈 (3-1) and 공고 탐색 (3-2), with worker preferences for the availability toggle. */
interface WorkerJobFeedRepository {
    /**
     * Searches with [filter]. When the filter has no coordinates, distance is measured from the
     * device's last known location, then the worker's registered base location.
     */
    suspend fun load(filter: JobFilter = JobFilter()): WorkerJobFeed

    /** Cached signup preference (name, radius, preferred types...). */
    suspend fun preference(refresh: Boolean = false): WorkerPreference
}

enum class SearchOrigin { FILTER, DEVICE, BASE_LOCATION, NONE }

data class WorkerJobFeed(
    val preference: WorkerPreference?,
    val origin: SearchOrigin,
    val latitude: Double?,
    val longitude: Double?,
    val jobs: List<WorkerJobPosting>,
) {
    val urgent: List<WorkerJobPosting> get() = jobs.filter { it.isUrgent }
    val applicable: List<WorkerJobPosting> get() = jobs.filter { JobEligibility.isApplicable(it, preference) }
}
