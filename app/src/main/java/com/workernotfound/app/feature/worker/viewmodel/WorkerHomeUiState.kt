package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/**
 * Immutable UI state for the worker home screen (UI spec 3-1).
 *
 * [isAvailableOnly] is the 근무 가능 토글 (default off): when on, only
 * immediately-applicable (urgent) openings are shown.
 */
data class WorkerHomeUiState(
    val isLoading: Boolean = true,
    val isAvailableOnly: Boolean = false,
    val urgentJobs: List<WorkerJobPosting> = emptyList(),
    val recommendedJobs: List<WorkerJobPosting> = emptyList(),
    val errorMessage: String? = null,
)
