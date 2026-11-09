package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.ServerSettlement

/** 정산 상태 (UI spec 2-4: 정산 내역 + 정산 확인). */
data class OwnerSettlementUiState(
    val isLoading: Boolean = true,
    val work: OwnerWork? = null,
    val serverSettlement: ServerSettlement? = null,
    val errorMessage: String? = null,
    val isConfirmDialogVisible: Boolean = false,
    val isProcessing: Boolean = false,
    val isApproved: Boolean = false,
    val actionErrorMessage: String? = null,
) {
    val canApprove: Boolean
        get() = serverSettlement != null && serverSettlement.paidAt == null && !isProcessing && !isApproved
}
