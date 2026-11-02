package com.workernotfound.app.feature.owner.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.domain.repository.OwnerApplicantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerApplicantsViewModel @Inject constructor(
    private val backend: BackendAccess,
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
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(t)) }
                }
        }
    }

    companion object {
        const val ARG_POSTING_ID = "postingId"
    }
}
