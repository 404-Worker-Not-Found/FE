package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.PastPosting

/** 지난 공고 목록 상태 (UI spec 2-6). */
data class OwnerPastPostingsUiState(
    val isLoading: Boolean = true,
    val postings: List<PastPosting> = emptyList(),
    val errorMessage: String? = null,
)
