package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.core.network.BackendAccess
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.owner.domain.repository.OwnerPastPostingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerPastPostingsViewModel @Inject constructor(
    private val repository: OwnerPastPostingRepository,
    private val backend: BackendAccess,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerPastPostingsUiState())
    val uiState: StateFlow<OwnerPastPostingsUiState> = _uiState.asStateFlow()

    /** Called on every resume so settled / closed postings appear right away. */
    fun refresh() {
        _uiState.update { it.copy(isLoading = it.postings.isEmpty() && it.errorMessage == null, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getPastPostings() }
                .onSuccess { postings -> _uiState.update { it.copy(isLoading = false, postings = postings) } }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(t)) }
                }
        }
    }
}
