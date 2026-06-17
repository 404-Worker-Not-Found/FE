package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.OwnerHomeSummary
import com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting

/** Immutable UI state for the owner home screen (UI spec 2-1). */
data class OwnerHomeUiState(
    val isLoading: Boolean = true,
    val summary: OwnerHomeSummary = OwnerHomeSummary(0, 0),
    val postings: List<OwnerJobPosting> = emptyList(),
    val errorMessage: String? = null,
)
