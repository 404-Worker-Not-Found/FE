package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.WorkerHomeRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import javax.inject.Inject

class WorkerHomeRepositoryImpl @Inject constructor(
    private val jobs: WorkerJobRepository,
) : WorkerHomeRepository {

    override suspend fun getUrgentJobs(): List<WorkerJobPosting> =
        jobs.searchJobs(JobFilter(urgentOnly = true)).urgent

    override suspend fun getRecommendedJobs(): List<WorkerJobPosting> =
        jobs.searchJobs(JobFilter()).recommended
}
