package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.JobEligibility
import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.model.WorkerPreference
import com.workernotfound.app.feature.worker.domain.repository.SearchOrigin

/** List vs map presentation for job search (UI spec 3-2: 리스트 보기 / 지도 보기). */
enum class JobViewMode { LIST, MAP }

/** How the submitted search text was applied. */
enum class SearchMode { NONE, STORE, AREA, NO_RESULT }

data class WorkerJobSearchUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val locating: Boolean = false,
    val locationError: Boolean = false,
    val viewMode: JobViewMode = JobViewMode.LIST,
    val jobs: List<WorkerJobPosting> = emptyList(),
    val preference: WorkerPreference? = null,
    val origin: SearchOrigin = SearchOrigin.NONE,
    val originLatitude: Double? = null,
    val originLongitude: Double? = null,
    val filter: JobFilter = JobFilter(),
    val isFilterSheetOpen: Boolean = false,
    val query: String = "",
    val submittedQuery: String = "",
    val searchMode: SearchMode = SearchMode.NONE,
    val errorMessage: String? = null,
) {
    /** Jobs after the store/category text search; area searches already re-centred the server query. */
    val visibleJobs: List<WorkerJobPosting>
        get() = if (searchMode == SearchMode.STORE) jobs.filter { it.matchesText(submittedQuery) } else jobs
    val urgentJobs: List<WorkerJobPosting> get() = visibleJobs.filter { it.isUrgent }
    val recommendedJobs: List<WorkerJobPosting>
        get() = visibleJobs.filter { !it.isUrgent && JobEligibility.isRecommended(it, preference) }
    val otherJobs: List<WorkerJobPosting>
        get() = visibleJobs.filter { !it.isUrgent && !JobEligibility.isRecommended(it, preference) }
    val totalCount: Int get() = visibleJobs.size
    val usesCurrentLocation: Boolean get() = filter.latitude != null && searchMode != SearchMode.AREA
}

internal fun WorkerJobPosting.matchesText(text: String): Boolean {
    val needle = text.trim()
    return needle.isNotEmpty() && (storeName.contains(needle, ignoreCase = true) || category.contains(needle, ignoreCase = true))
}
