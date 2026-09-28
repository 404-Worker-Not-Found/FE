package com.workernotfound.app.feature.owner.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerSettlementViewModel @Inject constructor(
    private val repository: OwnerWorkRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val workId: String = checkNotNull(savedStateHandle[ARG_WORK_ID]) {
        "workId argument is required for OwnerSettlementScreen"
    }

    private val _uiState = MutableStateFlow(OwnerSettlementUiState())
    val uiState: StateFlow<OwnerSettlementUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getWork(workId) to repository.getSettlement(workId) }
                .onSuccess { (work, settlement) ->
                    _uiState.update { it.copy(isLoading = false, work = work, settlement = settlement) }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "정산 내역을 불러오지 못했습니다.") }
                }
        }
    }

    fun requestApprove() = _uiState.update { it.copy(isConfirmDialogVisible = true) }

    fun dismissDialogs() = _uiState.update { it.copy(isConfirmDialogVisible = false, actionErrorMessage = null) }

    /** 정산 확인 → 지급 승인 (UI spec 2-4). */
    fun approve() {
        if (!_uiState.value.canApprove) return
        _uiState.update { it.copy(isConfirmDialogVisible = false, isProcessing = true) }
        viewModelScope.launch {
            runCatching { repository.confirmSettlement(workId) }
                .onSuccess { _uiState.update { it.copy(isProcessing = false, isApproved = true) } }
                .onFailure { t ->
                    _uiState.update { it.copy(isProcessing = false, actionErrorMessage = t.message ?: "지급 승인에 실패했습니다.") }
                }
        }
    }

    companion object {
        const val ARG_WORK_ID = "workId"
    }
}
