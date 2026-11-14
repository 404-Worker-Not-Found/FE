package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.WorkerApplication
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus

/** Application status + matching data source (UI spec 3-4, 3-5). */
interface WorkerApplicationRepository {
    suspend fun respondToMatching(applicationId: String, accept: Boolean): WorkerApplication
    suspend fun apply(jobId: String)
    suspend fun cancel(applicationId: String)
    suspend fun getApplications(): List<WorkerApplication>
    suspend fun getApplication(id: String): WorkerApplication
    /** Server application state per job; canceled and terminal applications cannot be resubmitted. */
    suspend fun applyStatusesByJobId(): Map<String, JobApplicationState>
}

data class JobApplicationState(val applicationId: String, val status: ApplyStatus)
