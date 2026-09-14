package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
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
class WorkerApplicationViewModel @Inject constructor(
    private val repository: WorkerApplicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerApplicationUiState())
    val uiState: StateFlow<WorkerApplicationUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getApplications() }
                .onSuccess { apps ->
                    _uiState.update { it.copy(isLoading = false, applications = apps) }
                    simulateRealtimeUpdate()
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = t.message ?: "지원 내역을 불러오지 못했습니다.")
                    }
                }
        }
    }

    /**
     * UI spec 3-4: 실시간 갱신 (원래 WebSocket). 데모에서는 타이머로 시뮬레이션 —
     * 잠시 후 '지원중' 건이 '매칭중'으로 새로고침 없이 바뀐다.
     */
    private fun simulateRealtimeUpdate() {
        viewModelScope.launch {
            delay(5000)
            _uiState.update { state ->
                state.copy(
                    applications = state.applications.map { app ->
                        if (app.status == ApplicationStatus.APPLYING) {
                            app.copy(status = ApplicationStatus.MATCHING)
                        } else {
                            app
                        }
                    },
                )
            }
        }
    }

    fun requestCancel(id: String) = _uiState.update { it.copy(pendingCancelId = id) }

    fun dismissCancel() = _uiState.update { it.copy(pendingCancelId = null) }

    /** 지원 취소: 목록에서 제거하지 않고 상태만 CANCELED로 표시 (communication diagram: setCanceled). */
    fun confirmCancel() {
        _uiState.update { state ->
            val id = state.pendingCancelId
            state.copy(
                applications = state.applications.map {
                    if (it.id == id) it.copy(status = ApplicationStatus.CANCELED) else it
                },
                pendingCancelId = null,
            )
        }
    }
}
