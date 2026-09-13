package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.WorkerApplication

/** 지원 현황 화면 상태 (UI spec 3-4). */
data class WorkerApplicationUiState(
    val isLoading: Boolean = true,
    val applications: List<WorkerApplication> = emptyList(),
    /** 지원 취소 확인 팝업 대상 (UI spec 3-4: 확인 팝업 제공). */
    val pendingCancelId: String? = null,
    val errorMessage: String? = null,
)
