package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.worker.domain.model.ChatMessage
import com.workernotfound.app.feature.worker.domain.repository.WorkerChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerChatRoomViewModel @Inject constructor(
    private val repository: WorkerChatRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val roomId: String = checkNotNull(savedStateHandle["roomId"]) {
        "roomId argument is required for WorkerChatRoomScreen"
    }

    private val _uiState = MutableStateFlow(WorkerChatRoomUiState())
    val uiState: StateFlow<WorkerChatRoomUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getRoom(roomId) to repository.getMessages(roomId) }
                .onSuccess { (room, messages) ->
                    _uiState.update { it.copy(isLoading = false, room = room, messages = messages) }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "대화를 불러오지 못했습니다.") }
                }
        }
    }

    fun onDraftChange(text: String) = _uiState.update { it.copy(draft = text) }

    /** 전송: 실제 서버 없이 로컬에 내 메시지를 추가 (mock, WebSocket은 후속). */
    fun send() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) return
        val newMessage = ChatMessage(
            id = "local_${System.currentTimeMillis()}",
            text = text,
            fromMe = true,
            timeText = "방금",
        )
        _uiState.update { it.copy(messages = it.messages + newMessage, draft = "") }
    }
}
