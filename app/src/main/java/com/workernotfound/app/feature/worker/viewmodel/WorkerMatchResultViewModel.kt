package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerMatchResultViewModel @Inject constructor(
    private val repository: WorkerApplicationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val applicationId: String = checkNotNull(savedStateHandle["applicationId"]) {
        "applicationId argument is required for WorkerMatchResultScreen"
    }

    private val _uiState = MutableStateFlow(WorkerMatchResultUiState())
    val uiState: StateFlow<WorkerMatchResultUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getApplication(applicationId) }
                .onSuccess { app ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            application = app,
                            remainingSeconds = if (app.hasRematchOffer) REMATCH_LIMIT_SECONDS else 0,
                        )
                    }
                    if (app.hasRematchOffer) startCountdown()
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = t.message ?: "매칭 정보를 불러오지 못했습니다.")
                    }
                }
        }
    }

    /** 재매칭 응답 제한시간 카운트다운. 0이 되면 시간 초과 처리 (UI spec 3-5). */
    private fun startCountdown() {
        viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 &&
                _uiState.value.outcome == RematchOutcome.PENDING
            ) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0)) }
            }
            if (_uiState.value.remainingSeconds == 0L && _uiState.value.outcome == RematchOutcome.PENDING) {
                _uiState.update { it.copy(outcome = RematchOutcome.EXPIRED) }
            }
        }
    }

    /** 재매칭 수락 → 즉시 새 근무자로 매칭 확정 (UI spec 3-5). */
    fun accept() = _uiState.update { it.copy(outcome = RematchOutcome.ACCEPTED) }

    fun reject() = _uiState.update { it.copy(outcome = RematchOutcome.REJECTED) }

    private companion object {
        const val REMATCH_LIMIT_SECONDS = 5 * 60L
    }
}
