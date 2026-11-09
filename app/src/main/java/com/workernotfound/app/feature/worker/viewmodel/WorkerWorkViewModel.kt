package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.core.network.BackendAccess
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.model.WorkStatus
import com.workernotfound.app.feature.worker.domain.repository.WorkerWorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerWorkViewModel @Inject constructor(
    private val repository: WorkerWorkRepository,
    private val backend: BackendAccess,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerWorkUiState())
    val uiState: StateFlow<WorkerWorkUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            runCatching { repository.getWorks() }
                .onSuccess { works ->
                    val (inProgress, scheduled) = works.partition { it.status == WorkStatus.IN_PROGRESS }
                    _uiState.update {
                        it.copy(isLoading = false, scheduled = scheduled, inProgress = inProgress)
                    }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(t)) }
                }
        }
    }
}
