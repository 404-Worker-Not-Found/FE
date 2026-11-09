package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.repository.WorkerTrustRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerTrustViewModel @Inject constructor(
    private val repository: WorkerTrustRepository,
    private val backend: BackendAccess,
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
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(t)) }
                }
        }
    }
}
