package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail

/**
 * UI state for job detail (UI spec 3-3). [remainingSeconds] is the live
 * countdown to work start, ticked by the ViewModel.
 */
data class WorkerJobDetailUiState(
    val isLoading: Boolean = true,
    val detail: WorkerJobDetail? = null,
    val remainingSeconds: Long = 0,
    val errorMessage: String? = null,
)
