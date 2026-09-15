package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.repository.WorkerTrustRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerTrustViewModel @Inject constructor(
    private val repository: WorkerTrustRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerTrustUiState())
    val uiState: StateFlow<WorkerTrustUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getTrustSummary() }
                .onSuccess { summary -> _uiState.update { it.copy(isLoading = false, summary = summary) } }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "신뢰도 정보를 불러오지 못했습니다.") }
                }
        }
    }
}
