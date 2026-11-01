package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerHomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerHomeViewModel @Inject constructor(
    private val applications: WorkerApplicationRepository,
    private val backend: BackendAccess,
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
                    if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = backend.message(throwable),
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
        viewModelScope.launch {
            try {
                applications.apply(jobId)
                _uiState.update { state ->
                    state.copy(
                        urgentJobs = state.urgentJobs.markApplied(jobId),
                        recommendedJobs = state.recommendedJobs.markApplied(jobId),
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = backend.message(error)) }
            }
        }
    }

    private fun List<WorkerJobPosting>.markApplied(jobId: String) =
        map { if (it.id == jobId) it.copy(applyStatus = ApplyStatus.APPLIED) else it }
}
