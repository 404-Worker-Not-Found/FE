package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.ChatMessage
import com.workernotfound.app.feature.worker.domain.repository.WorkerChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerChatRoomViewModel @Inject constructor(
    private val backend: BackendAccess,
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
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(t)) }
                }
        }
    }

    fun onDraftChange(text: String) = _uiState.update { it.copy(draft = text) }

    // Preserve the idempotency key when retrying the same draft after a lost response.
    private var pendingSend: Pair<String, String>? = null
    private var isSending = false

    fun send() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty() || isSending) return
        val key = pendingSend?.takeIf { it.second == text }?.first ?: java.util.UUID.randomUUID().toString()
        pendingSend = key to text
        isSending = true
        viewModelScope.launch {
            try {
                val message = repository.send(roomId, key, text)
                _uiState.update { state -> state.copy(
                    messages = (state.messages + message).distinctBy { it.id },
                    draft = if (state.draft.trim() == text) "" else state.draft,
                    errorMessage = null,
                ) }
                pendingSend = null
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = backend.message(error)) }
            } finally {
                isSending = false
            }
        }
    }
}
