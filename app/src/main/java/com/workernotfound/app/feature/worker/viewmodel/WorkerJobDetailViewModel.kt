package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerJobDetailViewModel @Inject constructor(
    private val repository: WorkerJobRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val jobId: String = checkNotNull(savedStateHandle["jobId"]) {
        "jobId argument is required for WorkerJobDetailScreen"
    }

    private val _uiState = MutableStateFlow(WorkerJobDetailUiState())
    val uiState: StateFlow<WorkerJobDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getJobDetail(jobId) }
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = detail,
                            remainingSeconds = detail.startsInSeconds,
                        )
                    }
                    startCountdown()
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

    /** Tick the work-start countdown once per second (UI spec 3-3: 남은 시간). */
    private fun startCountdown() {
        viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    /** 즉시 지원 (UI spec 3-3: 필수). */
    fun apply() {
        _uiState.update { state ->
            val applied = state.detail?.copy(applyStatus = ApplyStatus.APPLIED)
            state.copy(detail = applied ?: state.detail)
        }
    }
}
