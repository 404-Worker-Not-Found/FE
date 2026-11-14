package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.SearchOrigin

/**
 * Immutable UI state for the worker home screen (UI spec 3-1).
 *
 * [isAvailableOnly] is the 즉시 지원 가능 toggle (default off): when on, only openings the
 * worker can apply to right now (recruiting, before deadline, inside the activity radius) are shown.
 */
data class WorkerHomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val workerName: String? = null,
    val activityRadiusKm: Int? = null,
    val origin: SearchOrigin = SearchOrigin.NONE,
    val isAvailableOnly: Boolean = false,
    val urgentJobs: List<WorkerJobPosting> = emptyList(),
    val allJobs: List<WorkerJobPosting> = emptyList(),
    val applicableJobs: List<WorkerJobPosting> = emptyList(),
    val applyingJobIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
)
