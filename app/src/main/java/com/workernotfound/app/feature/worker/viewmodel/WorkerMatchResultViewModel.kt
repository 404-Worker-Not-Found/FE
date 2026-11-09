package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerMatchResultViewModel @Inject constructor(
    private val backend: BackendAccess,
    private val repository: WorkerApplicationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val applicationId: String = checkNotNull(savedStateHandle["applicationId"]) {
        "applicationId argument is required for WorkerMatchResultScreen"
    }

    private val _uiState = MutableStateFlow(WorkerMatchResultUiState())
    val uiState: StateFlow<WorkerMatchResultUiState> = _uiState.asStateFlow()

    private var loading: kotlinx.coroutines.Job? = null
    private var countdown: kotlinx.coroutines.Job? = null

    init {
        load()
    }

    fun load() = refresh(silent = false)
    fun poll() = refresh(silent = true)
    private fun refresh(silent: Boolean) {
        if (loading?.isActive == true || isResponding) return
        _uiState.update { it.copy(isLoading = if (silent) it.isLoading else true, errorMessage = null) }
        loading = viewModelScope.launch {
            runCatching { repository.getApplication(applicationId) }
                .onSuccess { app ->
                    countdown?.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            application = app,
                            outcome = if (it.application?.matchingId != app.matchingId) RematchOutcome.PENDING else it.outcome,
                            remainingSeconds = if (app.hasRematchOffer) app.proposalExpiresInSeconds ?: 0 else 0,
                        )
                    }
                    if (app.hasRematchOffer) startCountdown()
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = backend.message(t))
                    }
                }
        }
    }

    /** 재매칭 응답 제한시간 카운트다운. 0이 되면 시간 초과 처리 (UI spec 3-5). */
    private fun startCountdown() {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 &&
                _uiState.value.outcome == RematchOutcome.PENDING
            ) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0)) }
            }
            if (_uiState.value.remainingSeconds == 0L && _uiState.value.outcome == RematchOutcome.PENDING) {
                _uiState.update { it.copy(outcome = RematchOutcome.EXPIRED) }
            }
        }
    }

    /** 재매칭 수락 → 즉시 새 근무자로 매칭 확정 (UI spec 3-5). */
    private var isResponding = false
    fun accept() = respond(accept = true)
    fun reject() = respond(accept = false)

    private fun respond(accept: Boolean) {
        if (isResponding) return
        isResponding = true
        loading?.cancel()
        countdown?.cancel()
        viewModelScope.launch {
            try {
                val app = repository.respondToMatching(applicationId, accept)
                _uiState.update { it.copy(isLoading = false, application = app, errorMessage = null, outcome = if (accept) RematchOutcome.ACCEPTED else RematchOutcome.REJECTED) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(error)) }
            } finally { isResponding = false }
        }
    }
}
