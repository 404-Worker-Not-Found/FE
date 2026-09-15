package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.TrustSummary

/** 나의 신뢰도 화면 상태 (UI spec 4-2). */
data class WorkerTrustUiState(
    val isLoading: Boolean = true,
    val summary: TrustSummary? = null,
    val errorMessage: String? = null,
)
