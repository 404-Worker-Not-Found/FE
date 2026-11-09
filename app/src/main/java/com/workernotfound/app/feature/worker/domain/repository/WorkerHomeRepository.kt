package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/** Worker home data (UI spec 3-1). */
interface WorkerHomeRepository {
    suspend fun getUrgentJobs(): List<WorkerJobPosting>
    suspend fun getRecommendedJobs(): List<WorkerJobPosting>
}
