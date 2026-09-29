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
class OwnerApplicantsViewModel @Inject constructor(
    private val repository: OwnerApplicantRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val postingId: String = checkNotNull(savedStateHandle[ARG_POSTING_ID]) {
        "postingId argument is required for OwnerApplicantsScreen"
    }

    private val _uiState = MutableStateFlow(OwnerApplicantsUiState())
    val uiState: StateFlow<OwnerApplicantsUiState> = _uiState.asStateFlow()

    /** Called on every resume so a match confirmed on the detail screen shows up here. */
    fun refresh() {
        _uiState.update { it.copy(isLoading = it.board == null, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getApplicantBoard(postingId) }
                .onSuccess { board -> _uiState.update { it.copy(isLoading = false, board = board) } }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "지원자를 불러오지 못했습니다.") }
                }
        }
    }

    companion object {
        const val ARG_POSTING_ID = "postingId"
    }
}
