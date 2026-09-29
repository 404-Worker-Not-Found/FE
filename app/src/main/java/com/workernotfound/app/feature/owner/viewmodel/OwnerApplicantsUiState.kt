package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.ApplicantBoard

/** 지원자 목록 상태 (UI spec 2-3). */
data class OwnerApplicantsUiState(
    val isLoading: Boolean = true,
    val board: ApplicantBoard? = null,
    val errorMessage: String? = null,
)
