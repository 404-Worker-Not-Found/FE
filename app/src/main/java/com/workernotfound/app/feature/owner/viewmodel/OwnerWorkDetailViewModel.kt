package com.workernotfound.app.feature.owner.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerWorkDetailViewModel @Inject constructor(
    private val repository: OwnerWorkRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val workId: String = checkNotNull(savedStateHandle[ARG_WORK_ID]) {
        "workId argument is required for OwnerWorkDetailScreen"
    }

    private val _uiState = MutableStateFlow(OwnerWorkDetailUiState())
    val uiState: StateFlow<OwnerWorkDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<OwnerWorkDetailEvent>(Channel.BUFFERED)
    val events: Flow<OwnerWorkDetailEvent> = _events.receiveAsFlow()

    init {
        startTicker()
    }

    /** Called on every resume so returning from 정산 shows the latest status. */
    fun refresh() {
        _uiState.update { it.copy(isLoading = it.work == null, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getWork(workId) }
                .onSuccess { work -> _uiState.update { it.copy(isLoading = false, work = work) } }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "근무 정보를 불러오지 못했습니다.") }
                }
        }
    }

    fun requestComplete() = _uiState.update { it.copy(isCompleteDialogVisible = true) }

    fun requestNoShow() {
        if (_uiState.value.timing?.canConfirmNoShow == true) {
            _uiState.update { it.copy(isNoShowDialogVisible = true) }
        }
    }

    fun dismissDialogs() = _uiState.update {
        it.copy(isCompleteDialogVisible = false, isNoShowDialogVisible = false, actionErrorMessage = null)
    }

    /** 근무 완료 → 정산 트리거 (UI spec 2-4). */
    fun completeWork() = runAction(OwnerWorkDetailEvent.OpenSettlement(workId)) {
        repository.completeWork(workId)
    }

    /** 노쇼 확정 (UI spec 2-5), then hand over to automatic re-matching. */
    fun confirmNoShow() = runAction(OwnerWorkDetailEvent.NoShowConfirmed(workId)) {
        repository.confirmNoShow(workId)
    }

    fun openSettlement() {
        viewModelScope.launch { _events.send(OwnerWorkDetailEvent.OpenSettlement(workId)) }
    }

    private fun runAction(onSuccess: OwnerWorkDetailEvent, action: suspend () -> Unit) {
        if (_uiState.value.isProcessing) return
        _uiState.update { it.copy(isCompleteDialogVisible = false, isNoShowDialogVisible = false, isProcessing = true) }
        viewModelScope.launch {
            runCatching { action() }
                .onSuccess {
                    _uiState.update { it.copy(isProcessing = false) }
                    _events.send(onSuccess)
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isProcessing = false, actionErrorMessage = t.message ?: "처리하지 못했습니다.") }
                }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            var ticks = 0
            while (isActive) {
                delay(TICK_MILLIS)
                _uiState.update { it.copy(nowMillis = System.currentTimeMillis()) }
                if (++ticks % ATTENDANCE_POLL_TICKS == 0 && isAwaitingCheckIn()) pollAttendance()
            }
        }
    }

    private fun isAwaitingCheckIn(): Boolean {
        val work = _uiState.value.work ?: return false
        return work.status == WorkProgressStatus.ACTIVE && !work.isAttendanceVerified
    }

    /** Stand-in for a real-time GPS 출근 인증 update: silently re-reads the shift. */
    private suspend fun pollAttendance() {
        runCatching { repository.getWork(workId) }
            .onSuccess { work -> _uiState.update { it.copy(work = work) } }
    }

    companion object {
        const val ARG_WORK_ID = "workId"
        private const val TICK_MILLIS = 1_000L
        private const val ATTENDANCE_POLL_TICKS = 5
    }
}
