package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.WorkSchedule

/** 근무 관리 상태 (UI spec 3-6). */
data class WorkerWorkUiState(
    val isLoading: Boolean = true,
    val scheduled: List<WorkSchedule> = emptyList(),
    val inProgress: List<WorkSchedule> = emptyList(),
    val errorMessage: String? = null,
)
