package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/**
 * Worker home data (UI spec 3-1). Backed by a mock source for the demo; swap the
 * implementation for a network-backed one when the backend contract is confirmed
 * (decision: Demo Scope and Mock-First Strategy).
 */
interface WorkerHomeRepository {
    suspend fun getUrgentJobs(): List<WorkerJobPosting>
    suspend fun getRecommendedJobs(): List<WorkerJobPosting>
}
