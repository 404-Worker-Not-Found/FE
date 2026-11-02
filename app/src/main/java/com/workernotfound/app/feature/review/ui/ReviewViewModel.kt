package com.workernotfound.app.feature.review.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.review.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class ReviewUiState(val pending: List<ReviewTarget> = emptyList(), val received: List<ReceivedReview> = emptyList(),
    val pendingNext: Int? = null, val receivedNext: Int? = null, val count: Long = 0, val average: BigDecimal? = null,
    val selected: ReviewTarget? = null, val rating: Int = 0, val comment: String = "", val attempted: Boolean = false,
    val busy: Boolean = false, val written: Boolean = false, val error: String? = null) {
    override fun toString() = "ReviewUiState(busy=$busy, written=$written)"
}
@HiltViewModel
class ReviewViewModel @Inject constructor(private val repository: ReviewRepository,
    private val backend: BackendAccess) : ViewModel() {
    private val state = MutableStateFlow(ReviewUiState())
    val uiState = state.asStateFlow()
    init { load() }
    fun load() = action {
        val pending = repository.pending()
        val received = repository.received()
        state.update { it.copy(pending = pending.items, pendingNext = pending.next, received = received.items,
            receivedNext = received.next, count = received.count, average = received.average) }
    }
    fun more(pending: Boolean) = action {
        if (pending) {
            val page = repository.pending(state.value.pendingNext ?: return@action)
            state.update { it.copy(pending = (it.pending + page.items).distinctBy { row -> row.workId }, pendingNext = page.next) }
        } else {
            val page = repository.received(state.value.receivedNext ?: return@action)
            state.update { it.copy(received = (it.received + page.items).distinctBy { row -> row.id }, receivedNext = page.next, count = page.count, average = page.average) }
        }
    }
    fun select(target: ReviewTarget?) { if (!state.value.busy) state.update { it.copy(selected = target, rating = 0, comment = "", attempted = false, error = null, written = false) } }
    fun rating(value: Int) { if (!state.value.busy && !state.value.attempted) state.update { it.copy(rating = value) } }
    fun comment(value: String) { if (!state.value.busy && !state.value.attempted) state.update { it.copy(comment = value) } }
    fun write() {
        val s = state.value
        val target = s.selected ?: return
        if (s.rating !in 1..5 || s.comment.length > 500) return
        state.update { it.copy(attempted = true) }
        action {
            repository.write(target, s.rating, s.comment)
            state.update { it.copy(pending = it.pending.filterNot { row -> row.workId == target.workId },
                selected = null, comment = "", rating = 0, attempted = false, written = true) }
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
