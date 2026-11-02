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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    private val syncMutex = Mutex()
    private var syncedThrough = 0L
    private var updates: Job? = null
    private var polling: Job? = null
    private var reading: Job? = null
    private var visibleThrough = 0L
    private var acknowledgedRead = 0L
    private var loading: Job? = null
    private var foreground = false

    fun startUpdates() {
        foreground = true
        if (com.workernotfound.app.BuildConfig.USE_DEMO_DATA || updates?.isActive == true) return
        updates = viewModelScope.launch {
            repository.changes(roomId).retryWhen { _, attempt ->
                _uiState.update { it.copy(reconnecting = true) }
                delay(minOf(30_000L, 1000L * (1L shl attempt.coerceAtMost(5).toInt())))
                true
            }.conflate().collect {
                _uiState.update { it.copy(reconnecting = false) }
                synchronize()
            }
        }
        polling = viewModelScope.launch { while (true) { synchronize(); delay(15_000) } }
    }
    fun stopUpdates() { foreground = false; updates?.cancel(); polling?.cancel(); reading?.cancel() }
    private suspend fun synchronize() = syncMutex.withLock {
        if (_uiState.value.isLoading || _uiState.value.room == null) return@withLock
        try {
            val messages = repository.sync(roomId, syncedThrough)
            val room = repository.getRoom(roomId)
            messages.mapNotNull { it.id.toLongOrNull() }.maxOrNull()?.let { syncedThrough = maxOf(syncedThrough, it) }
            _uiState.update { it.copy(room = room, messages = (it.messages + messages).distinctBy { row -> row.id }.sortedBy { row -> row.id.toLongOrNull() ?: 0 }) }
            visible(visibleThrough)
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { _uiState.update { it.copy(errorMessage = backend.message(e)) } }
    }
    fun visible(id: Long) {
        if (!foreground || id <= 0 || com.workernotfound.app.BuildConfig.USE_DEMO_DATA) return
        visibleThrough = maxOf(visibleThrough, id)
        if (reading?.isActive == true || visibleThrough <= acknowledgedRead) return
        reading = viewModelScope.launch {
            try { while (foreground && visibleThrough > acknowledgedRead) acknowledgedRead = repository.read(roomId, visibleThrough) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(errorMessage = backend.message(e)) } }
        }
    }

    init {
        load()
    }

    fun load() {
        if (loading?.isActive == true) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loading = viewModelScope.launch {
            syncMutex.withLock {
            runCatching { repository.getRoom(roomId) to repository.getMessages(roomId) }
                .onSuccess { (room, messages) ->
                    syncedThrough = messages.mapNotNull { it.id.toLongOrNull() }.maxOrNull() ?: 0
                    _uiState.update { it.copy(isLoading = false, room = room, messages = messages) }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(t)) }
                }
            }
            if (foreground) synchronize()
        }
    }

    fun onDraftChange(text: String) = _uiState.update { it.copy(draft = text.take(2000)) }

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
