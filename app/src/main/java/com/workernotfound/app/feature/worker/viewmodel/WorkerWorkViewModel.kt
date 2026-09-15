package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.model.WorkStatus
import com.workernotfound.app.feature.worker.domain.repository.WorkerWorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerWorkViewModel @Inject constructor(
    private val repository: WorkerWorkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerWorkUiState())
    val uiState: StateFlow<WorkerWorkUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getWorks() }
                .onSuccess { works ->
                    val (inProgress, scheduled) = works.partition { it.status == WorkStatus.IN_PROGRESS }
                    _uiState.update {
                        it.copy(isLoading = false, scheduled = scheduled, inProgress = inProgress)
                    }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "근무 정보를 불러오지 못했습니다.") }
                }
        }
    }
}
