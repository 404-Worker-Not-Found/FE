package com.workernotfound.app.feature.owner.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.owner.domain.repository.OwnerApplicantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerApplicantDetailViewModel @Inject constructor(
    private val repository: OwnerApplicantRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val postingId: String = checkNotNull(savedStateHandle[ARG_POSTING_ID]) {
        "postingId argument is required for OwnerApplicantDetailScreen"
    }
    private val applicantId: String = checkNotNull(savedStateHandle[ARG_APPLICANT_ID]) {
        "applicantId argument is required for OwnerApplicantDetailScreen"
    }

    private val _uiState = MutableStateFlow(OwnerApplicantDetailUiState())
    val uiState: StateFlow<OwnerApplicantDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = it.detail == null, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getApplicantDetail(postingId, applicantId) }
                .onSuccess { detail -> _uiState.update { it.copy(isLoading = false, detail = detail) } }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "지원자 정보를 불러오지 못했습니다.") }
                }
        }
    }

    fun requestConfirm() = _uiState.update { it.copy(isConfirmDialogVisible = true, actionErrorMessage = null) }

    fun dismissConfirm() = _uiState.update { it.copy(isConfirmDialogVisible = false) }

    fun dismissActionError() = _uiState.update { it.copy(actionErrorMessage = null) }

    /** 매칭 확정 (UI spec 2-3). Chat room creation / notification are backend-side. */
    fun confirmMatch() {
        if (_uiState.value.isConfirming) return
        _uiState.update { it.copy(isConfirmDialogVisible = false, isConfirming = true) }
        viewModelScope.launch {
            runCatching { repository.confirmMatch(postingId, applicantId) }
                .onSuccess { workId ->
                    _uiState.update { it.copy(isConfirming = false, matchedWorkId = workId) }
                    load()
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(isConfirming = false, actionErrorMessage = t.message ?: "매칭을 확정하지 못했습니다.")
                    }
                }
        }
    }

    companion object {
        const val ARG_POSTING_ID = "postingId"
        const val ARG_APPLICANT_ID = "applicantId"
    }
}
