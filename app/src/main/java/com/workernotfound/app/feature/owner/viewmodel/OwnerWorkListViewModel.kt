package com.workernotfound.app.feature.owner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerWorkListViewModel @Inject constructor(
    private val repository: OwnerWorkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerWorkListUiState())
    val uiState: StateFlow<OwnerWorkListUiState> = _uiState.asStateFlow()

    init {
        startTicker()
    }

    /** Called on every resume so completed / no-show shifts drop out of the list. */
    fun refresh() {
        _uiState.update { it.copy(isLoading = it.works.isEmpty() && it.errorMessage == null, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getActiveWorks() }
                .onSuccess { works -> _uiState.update { it.copy(isLoading = false, works = works) } }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "근무 현황을 불러오지 못했습니다.") }
                }
        }
    }

    /** 근무 진행 타이머: re-evaluates elapsed time and button conditions every second. */
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

    private fun isAwaitingCheckIn(): Boolean = _uiState.value.works.any {
        it.status == WorkProgressStatus.ACTIVE && !it.isAttendanceVerified
    }

    /** Stand-in for real-time GPS 출근 인증 updates: silently re-reads the list. */
    private suspend fun pollAttendance() {
        runCatching { repository.getActiveWorks() }
            .onSuccess { works -> _uiState.update { it.copy(works = works) } }
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
        const val ATTENDANCE_POLL_TICKS = 5
    }
}
