package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.ApplicantDetail

/** 지원자 상세 상태 (UI spec 2-3). */
data class OwnerApplicantDetailUiState(
    val isLoading: Boolean = true,
    val detail: ApplicantDetail? = null,
    val errorMessage: String? = null,
    val isConfirmDialogVisible: Boolean = false,
    val isConfirming: Boolean = false,
    /** Set once the match is confirmed; drives the success dialog. */
    val matchedWorkId: String? = null,
    val actionErrorMessage: String? = null,
)
