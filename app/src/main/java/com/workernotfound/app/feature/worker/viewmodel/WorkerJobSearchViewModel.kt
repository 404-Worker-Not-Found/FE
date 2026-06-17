package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerJobSearchViewModel @Inject constructor(
    private val repository: WorkerJobRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerJobSearchUiState())
    val uiState: StateFlow<WorkerJobSearchUiState> = _uiState.asStateFlow()

    init {
        search(JobFilter())
    }

    private fun search(filter: JobFilter) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.searchJobs(filter) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            urgentJobs = result.urgent,
                            recommendedJobs = result.recommended,
                            filter = filter,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "공고를 불러오지 못했습니다.",
                        )
                    }
                }
        }
    }

    fun setViewMode(mode: JobViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun openFilterSheet() = _uiState.update { it.copy(isFilterSheetOpen = true) }

    fun dismissFilterSheet() = _uiState.update { it.copy(isFilterSheetOpen = false) }

    fun applyFilter(filter: JobFilter) {
        _uiState.update { it.copy(isFilterSheetOpen = false) }
        search(filter)
    }

    fun retry() = search(_uiState.value.filter)

    /** 즉시 지원 (UI spec 3-2: 성공 시 지원 완료 상태). */
    fun applyTo(jobId: String) {
        _uiState.update { state ->
            state.copy(
                urgentJobs = state.urgentJobs.markApplied(jobId),
                recommendedJobs = state.recommendedJobs.markApplied(jobId),
            )
        }
    }

    private fun List<WorkerJobPosting>.markApplied(jobId: String) =
        map { if (it.id == jobId) it.copy(applyStatus = ApplyStatus.APPLIED) else it }
}
