package com.workernotfound.app.feature.notification.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.notification.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotificationUiState(val items: List<AppNotification> = emptyList(), val next: Long? = null,
    val unread: Long = 0, val busy: Boolean = false, val error: String? = null)
@HiltViewModel
class NotificationViewModel @Inject constructor(private val repository: NotificationRepository,
    private val backend: BackendAccess) : ViewModel() {
    private val state = MutableStateFlow(NotificationUiState())
    val uiState = state.asStateFlow()
    init { load() }
    fun load(more: Boolean = false) = action {
        val before = if (more) state.value.next ?: return@action else null
        val page = repository.page(before)
        val unread = repository.unreadCount()
        state.update { it.copy(items = (if (more) it.items + page.items else page.items).distinctBy { item -> item.id }, next = page.next, unread = unread) }
    }
    fun read(item: AppNotification, onOpen: (AppNotification) -> Unit) = action {
        val updated = if (item.read) item else repository.markRead(item.id)
        state.update { it.copy(items = it.items.map { row -> if (row.id == updated.id) updated else row },
            unread = if (item.read) it.unread else (it.unread - 1).coerceAtLeast(0)) }
        onOpen(updated)
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
