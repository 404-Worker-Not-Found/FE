package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/** List vs map presentation for job search (UI spec 3-2: 리스트 보기 / 지도 보기). */
enum class JobViewMode { LIST, MAP }

data class WorkerJobSearchUiState(
    val isLoading: Boolean = true,
    val viewMode: JobViewMode = JobViewMode.LIST,
    val urgentJobs: List<WorkerJobPosting> = emptyList(),
    val recommendedJobs: List<WorkerJobPosting> = emptyList(),
    val filter: JobFilter = JobFilter(),
    val isFilterSheetOpen: Boolean = false,
    val errorMessage: String? = null,
) {
    val totalCount: Int get() = urgentJobs.size + recommendedJobs.size
}
