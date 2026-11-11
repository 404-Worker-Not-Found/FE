package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobFeedRepository
import com.workernotfound.app.feature.worker.domain.model.JobEligibility
import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail
import com.workernotfound.app.feature.worker.domain.model.availability
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerJobDetailViewModel @Inject constructor(
    private val applications: WorkerApplicationRepository,
    private val backend: BackendAccess,
    private val repository: WorkerJobRepository,
    private val feed: WorkerJobFeedRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val jobId: String = checkNotNull(savedStateHandle["jobId"]) {
        "jobId argument is required for WorkerJobDetailScreen"
    }

    private val _uiState = MutableStateFlow(WorkerJobDetailUiState())
    val uiState: StateFlow<WorkerJobDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private var countdownJob: Job? = null
    private var loadJob: Job? = null

    fun load() {
        countdownJob?.cancel()
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            runCatching { enrich(repository.getJobDetail(jobId)) }
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = detail,
                            remainingSeconds = detail.startsInSeconds,
                        )
                    }
                    startCountdown()
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = backend.message(throwable),
                        )
                    }
                }
        }
    }

    /** Adds the fields the detail API lacks from the search card the worker opened, plus their radius. */
    private suspend fun enrich(detail: WorkerJobDetail): WorkerJobDetail {
        val card = repository.cachedCard(jobId)
        val preference = try { feed.preference() } catch (e: CancellationException) { throw e } catch (_: Exception) { null }
        return detail.copy(
            isUrgent = card?.isUrgent ?: detail.isUrgent,
            distanceKm = card?.distanceKm,
            activityRadiusKm = preference?.activityRadiusKm,
            availability = JobEligibility.availability(card, preference),
            applyStatus = card?.applyStatus ?: detail.applyStatus,
        )
    }

    /** Tick the work-start countdown once per second (UI spec 3-3: 남은 시간). */
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    /** 즉시 지원 (UI spec 3-3: 필수). */
    fun apply() {
        viewModelScope.launch {
            try {
                applications.apply(this@WorkerJobDetailViewModel.jobId)
                _uiState.update { state ->
                    val applied = state.detail?.copy(applyStatus = ApplyStatus.APPLIED)
                    state.copy(detail = applied ?: state.detail)
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = backend.message(error)) }
            }
        }
    }
}
