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

@HiltViewModel
class WorkerChatListViewModel @Inject constructor(
    private val repository: WorkerChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerChatListUiState())
    val uiState: StateFlow<WorkerChatListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getRooms() }
                .onSuccess { rooms -> _uiState.update { it.copy(isLoading = false, rooms = rooms) } }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "채팅 목록을 불러오지 못했습니다.") }
                }
        }
    }
}
