package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.OwnerWork

/** 근무 관리 탭 상태 (UI spec 2-4: 근무 현황 목록). */
data class OwnerWorkListUiState(
    val isLoading: Boolean = true,
    val works: List<OwnerWork> = emptyList(),
    val nowMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
) {
    val timings: List<WorkTiming> get() = works.map { WorkTiming(it, nowMillis) }
}
