package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/** Job search (3-2) and detail (3-3) data. */
interface WorkerJobRepository {
    /** Every opening matching [filter] across all result pages. */
    suspend fun searchJobs(filter: JobFilter): JobSearchResult

    suspend fun getJobDetail(id: String): WorkerJobDetail

    /** The search card last seen for [id]; it carries fields the detail API lacks (urgency, distance, deadline). */
    fun cachedCard(id: String): WorkerJobPosting?
}

data class JobSearchResult(val jobs: List<WorkerJobPosting>) {
    val totalCount: Int get() = jobs.size
}
