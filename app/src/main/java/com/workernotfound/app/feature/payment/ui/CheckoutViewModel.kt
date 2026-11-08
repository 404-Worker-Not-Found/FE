package com.workernotfound.app.feature.payment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.payment.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CheckoutUiState(val busy: Boolean = false, val checkout: Checkout? = null, val error: String? = null,
    val failed: Boolean = false, val retryConfirmation: Boolean = false, val revision: Int = 0)
@HiltViewModel
class CheckoutViewModel @Inject constructor(private val repository: CheckoutRepository,
    private val backend: BackendAccess) : ViewModel() {
    private val state = MutableStateFlow(CheckoutUiState())
    val uiState = state.asStateFlow()
    private var pending: Pair<Checkout, CheckoutReceipt>? = null
    fun open(jobId: Long) {
        if (state.value.busy || pending != null) return
        action { val checkout = repository.prepare(jobId); state.update { it.copy(checkout = checkout, failed = false) } }
    }
    fun cancel() { if (!state.value.busy) state.update { it.copy(checkout = null, failed = true) } }
    fun result(result: CheckoutReturn) {
        val checkout = state.value.checkout ?: return
        state.update { it.copy(checkout = null) }
        when (result) {
            CheckoutReturn.Failed -> state.update { it.copy(failed = true) }
            is CheckoutReturn.Success -> { pending = checkout to result.receipt; retry() }
        }
    }
    fun invalid() { state.update { it.copy(checkout = null, failed = true) } }
    fun retry() {
        val (checkout, receipt) = pending ?: return
        action {
            repository.confirm(checkout, receipt)
            pending = null
            state.update { it.copy(retryConfirmation = false, revision = it.revision + 1) }
        }
    }
    private fun action(block: suspend () -> Unit) {
        if (state.value.busy) return
        state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state.update { it.copy(error = backend.message(if (e is IllegalArgumentException || e is IllegalStateException)
                com.workernotfound.app.core.network.ApiException("CHECKOUT_NOT_READY") else e), retryConfirmation = pending != null) } }
            finally { state.update { it.copy(busy = false) } }
        }
    }
}
