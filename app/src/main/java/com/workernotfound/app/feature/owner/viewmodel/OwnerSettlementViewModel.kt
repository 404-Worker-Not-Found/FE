package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.domain.repository.OwnerSettlementRepository
import kotlinx.coroutines.CancellationException
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
    private val settlements: OwnerSettlementRepository,
    private val backend: BackendAccess,
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
            try {
                val work = repository.getWork(workId)
                if (BuildConfig.USE_DEMO_DATA) {
                    val settlement = repository.getSettlement(workId)
                    _uiState.update { it.copy(isLoading = false, work = work, settlement = settlement) }
                } else {
                    val settlement = settlements.getForWork(workId)
                    _uiState.update { it.copy(isLoading = false, work = work, serverSettlement = settlement) }
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(error)) }
            }
        }
    }

    fun requestApprove() = _uiState.update { it.copy(isConfirmDialogVisible = true) }

    fun dismissDialogs() = _uiState.update { it.copy(isConfirmDialogVisible = false, actionErrorMessage = null) }

    /** 정산 확인 → 지급 승인 (UI spec 2-4). */
    fun approve() {
        if (!BuildConfig.USE_DEMO_DATA || !_uiState.value.canApprove) return
        _uiState.update { it.copy(isConfirmDialogVisible = false, isProcessing = true) }
        viewModelScope.launch {
            runCatching { repository.confirmSettlement(workId) }
                .onSuccess { _uiState.update { it.copy(isProcessing = false, isApproved = true) } }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isProcessing = false, actionErrorMessage = backend.message(t)) }
                }
        }
    }

    companion object {
        const val ARG_WORK_ID = "workId"
    }
}
