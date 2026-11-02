package com.workernotfound.app.feature.cancellation.ui

import androidx.lifecycle.*
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.cancellation.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CancellationIntent(val action: CancellationAction, val id: Long? = null)
data class CancellationUiState(val context: CancellationContext? = null, val reason: String = "",
    val key: String? = null, val confirm: CancellationIntent? = null, val busy: Boolean = false,
    val now: Long = System.currentTimeMillis(), val error: String? = null)
@HiltViewModel
class CancellationViewModel @Inject constructor(private val repository: CancellationRepository,
    private val backend: BackendAccess, private val saved: SavedStateHandle) : ViewModel() {
    private val workId = checkNotNull(saved.get<String>("workId")).toLong()
    private val state = MutableStateFlow(CancellationUiState(key = saved["cancellationKey"]))
    val uiState = state.asStateFlow()
    init {
        load()
        viewModelScope.launch { while (true) { delay(1000); state.update { it.copy(now = System.currentTimeMillis()) } } }
    }
    fun load() = action {
        val context = repository.context(workId)
        state.update { it.copy(context = context) }
    }
    fun reason(value: String) { if (!state.value.busy && state.value.key == null) state.update { it.copy(reason = value) } }
    fun confirm(intent: CancellationIntent?) { if (!state.value.busy) state.update { it.copy(confirm = intent) } }
    fun perform() {
        val s = state.value
        val intent = s.confirm ?: return
        val context = s.context ?: return
        if (!context.withinWindow(System.currentTimeMillis())) return
        if (intent.action == CancellationAction.REQUEST && (!context.canRequest(System.currentTimeMillis()) || s.reason.isBlank() || s.reason.length > 500)) return
        val request = intent.id?.let { id -> context.requests.firstOrNull { it.id == id } }
        if (intent.action == CancellationAction.WITHDRAW && request?.canWithdraw(context.member) != true) return
        if (intent.action in setOf(CancellationAction.APPROVE, CancellationAction.REJECT) && request?.canRespond(context.member) != true) return
        state.update { it.copy(confirm = null) }
        val key = s.key ?: UUID.randomUUID().toString()
        if (intent.action == CancellationAction.REQUEST) {
            saved["cancellationKey"] = key
            state.update { it.copy(key = key) }
        }
        action {
            val updated = if (intent.action == CancellationAction.REQUEST) repository.request(workId, key, s.reason)
                else repository.respond(workId, requireNotNull(intent.id), intent.action)
            saved.remove<String>("cancellationKey")
            state.update { current -> current.copy(reason = "", key = null, context = current.context?.copy(
                requests = (current.context.requests.filterNot { it.id == updated.id } + updated).sortedByDescending { it.id })) }
            val refreshed = repository.context(workId)
            state.update { it.copy(context = refreshed) }
        }
    }
    private fun action(block: suspend () -> Unit) {
        if (state.value.busy) return
        state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state.update { it.copy(error = backend.message(e)) } }
            finally { state.update { it.copy(busy = false) } }
        }
    }
}
