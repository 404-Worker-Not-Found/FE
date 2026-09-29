package com.workernotfound.app.feature.owner.viewmodel

import androidx.lifecycle.SavedStateHandle
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
class OwnerPastPostingDetailViewModel @Inject constructor(
    private val repository: OwnerPastPostingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val postingId: String = checkNotNull(savedStateHandle[ARG_POSTING_ID]) {
        "postingId argument is required for OwnerPastPostingDetailScreen"
    }

    private val _uiState = MutableStateFlow(OwnerPastPostingDetailUiState())
    val uiState: StateFlow<OwnerPastPostingDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getPastPosting(postingId) }
                .onSuccess { posting ->
                    _uiState.update {
                        it.copy(isLoading = false, posting = posting, nowMillis = System.currentTimeMillis())
                    }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "지난 공고를 불러오지 못했습니다.") }
                }
        }
    }

    companion object {
        const val ARG_POSTING_ID = "postingId"
    }
}
