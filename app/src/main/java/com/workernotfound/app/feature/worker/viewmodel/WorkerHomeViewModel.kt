package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.WorkerHomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerHomeViewModel @Inject constructor(
    private val repository: WorkerHomeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerHomeUiState())
    val uiState: StateFlow<WorkerHomeUiState> = _uiState.asStateFlow()

    init {
        loadJobs()
    }

    fun loadJobs() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                val urgent = repository.getUrgentJobs()
                val recommended = repository.getRecommendedJobs()
                urgent to recommended
            }.onSuccess { (urgent, recommended) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        urgentJobs = urgent,
                        recommendedJobs = recommended,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "공고를 불러오지 못했습니다.",
                    )
                }
            }
        }
    }

    /** 근무 가능 토글 (UI spec 3-1). */
    fun toggleAvailableOnly(enabled: Boolean) {
        _uiState.update { it.copy(isAvailableOnly = enabled) }
    }

    /** 즉시 지원: mark the posting as applied (UI spec 3-1: 성공 시 지원 완료 상태). */
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
