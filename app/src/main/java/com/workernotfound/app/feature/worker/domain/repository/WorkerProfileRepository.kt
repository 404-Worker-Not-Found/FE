package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.WorkerProfile
import kotlinx.coroutines.flow.StateFlow

interface WorkerProfileRepository {
    val profile: StateFlow<WorkerProfile?>
    suspend fun load()
    suspend fun updateProfile(profile: WorkerProfile)
}
