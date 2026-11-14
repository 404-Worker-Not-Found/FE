package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.JobEligibility
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.model.WorkerPreference
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobFeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerHomeViewModel @Inject constructor(
    private val applications: WorkerApplicationRepository,
    private val backend: BackendAccess,
    private val feed: WorkerJobFeedRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerHomeUiState())
    val uiState: StateFlow<WorkerHomeUiState> = _uiState.asStateFlow()
    private var preference: WorkerPreference? = null
    private var loading: kotlinx.coroutines.Job? = null

    init {
        loadJobs()
    }

    /** Reload when the tab is shown again or pulled; keeps the current list visible meanwhile. */
    fun refresh() {
        if (loading?.isActive == true) return
        loadJobs(silent = true)
    }

    fun loadJobs(silent: Boolean = false) {
        loading?.cancel()
        _uiState.update { if (silent) it.copy(isRefreshing = true) else it.copy(isLoading = true, errorMessage = null) }
        loading = viewModelScope.launch {
            try {
                val result = feed.load()
                preference = result.preference
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                        workerName = result.preference?.name,
                        activityRadiusKm = result.preference?.activityRadiusKm,
                        origin = result.origin,
                        urgentJobs = result.urgent,
                        allJobs = result.jobs,
                        applicableJobs = result.applicable,
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = backend.message(error)) }
            }
        }
    }

    /** 즉시 지원 가능 토글 (UI spec 3-1, 기본값 off). */
    fun toggleAvailableOnly(enabled: Boolean) {
        _uiState.update { it.copy(isAvailableOnly = enabled) }
    }

    /** 즉시 지원: mark the posting as applied after the server accepts it. */
    fun applyTo(jobId: String) {
        if (jobId in _uiState.value.applyingJobIds ||
            _uiState.value.allJobs.none { it.id == jobId && it.applyStatus == ApplyStatus.AVAILABLE }) return
        _uiState.update { it.copy(applyingJobIds = it.applyingJobIds + jobId) }
        viewModelScope.launch {
            try {
                applications.apply(jobId)
                _uiState.update { state ->
                    state.copy(
                        urgentJobs = state.urgentJobs.markApplied(jobId),
                        allJobs = state.allJobs.markApplied(jobId),
                        // An applied posting is no longer "immediately applicable".
                        applicableJobs = state.applicableJobs.markApplied(jobId)
                            .filter { JobEligibility.isApplicable(it, preference) },
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = backend.message(error)) }
            } finally {
                _uiState.update { it.copy(applyingJobIds = it.applyingJobIds - jobId) }
            }
        }
    }

    private fun List<WorkerJobPosting>.markApplied(jobId: String) =
        map { if (it.id == jobId) it.copy(applyStatus = ApplyStatus.APPLIED) else it }
}
