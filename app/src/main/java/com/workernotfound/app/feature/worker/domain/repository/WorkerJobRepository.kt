package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/**
 * Job search (3-2) and detail (3-3) data. Mock-backed for the demo; swap for a
 * network-backed implementation when the backend contract is confirmed
 * (decision: Demo Scope and Mock-First Strategy).
 */
interface WorkerJobRepository {
    /** Openings matching [filter], already split into urgent and recommended. */
    suspend fun searchJobs(filter: JobFilter): JobSearchResult

    suspend fun getJobDetail(id: String): WorkerJobDetail
}

/** Search result grouped as the 공고 탐색 list shows it (UI spec 3-2). */
data class JobSearchResult(
    val urgent: List<WorkerJobPosting>,
    val recommended: List<WorkerJobPosting>,
) {
    val totalCount: Int get() = urgent.size + recommended.size
}
