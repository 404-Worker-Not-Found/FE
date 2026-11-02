package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.repository.WorkerChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.workernotfound.app.core.network.BackendAccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

@HiltViewModel
class WorkerChatListViewModel @Inject constructor(
    private val repository: WorkerChatRepository,
    private val backend: BackendAccess,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerChatListUiState())
    val uiState: StateFlow<WorkerChatListUiState> = _uiState.asStateFlow()
    private var loading: Job? = null

    init {
        load()
    }

    fun load() {
        if (loading?.isActive == true) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loading = viewModelScope.launch {
            runCatching { repository.getRooms() }
                .onSuccess { rooms -> _uiState.update { it.copy(isLoading = false, rooms = rooms) } }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(t)) }
                }
        }
    }
}
