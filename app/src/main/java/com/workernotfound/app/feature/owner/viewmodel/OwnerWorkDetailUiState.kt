package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.OwnerWork

/** 근무 현황 상태 (UI spec 2-4). */
data class OwnerWorkDetailUiState(
    val isLoading: Boolean = true,
    val work: OwnerWork? = null,
    val nowMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val isCompleteDialogVisible: Boolean = false,
    val isNoShowDialogVisible: Boolean = false,
    val isProcessing: Boolean = false,
    val actionErrorMessage: String? = null,
) {
    val timing: WorkTiming? get() = work?.let { WorkTiming(it, nowMillis) }
}

/** One-time navigation events from the 근무 현황 screen. */
sealed interface OwnerWorkDetailEvent {
    data class OpenSettlement(val workId: String) : OwnerWorkDetailEvent
    data class NoShowConfirmed(val workId: String) : OwnerWorkDetailEvent
}
