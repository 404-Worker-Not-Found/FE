package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerJobSearchViewModel @Inject constructor(
    private val applications: WorkerApplicationRepository,
    private val backend: BackendAccess,
    private val repository: WorkerJobRepository,
    private val positions: com.workernotfound.app.core.location.DeviceLocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerJobSearchUiState())
    val uiState: StateFlow<WorkerJobSearchUiState> = _uiState.asStateFlow()

    init {
        search(JobFilter())
    }

    private var searchJob: kotlinx.coroutines.Job? = null
    private var locationJob: kotlinx.coroutines.Job? = null

    fun locate() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            _uiState.update { it.copy(locating = true, locationError = false) }
            try {
                val point = positions.current()
                _uiState.update { it.copy(locating = false) }
                search(_uiState.value.filter.copy(latitude = point.latitude, longitude = point.longitude))
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { locationDenied() }
        }
    }

    fun locationDenied() { _uiState.update { it.copy(locating = false, locationError = true) } }

    private fun search(filter: JobFilter) {
        searchJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null, filter = filter) }
        searchJob = viewModelScope.launch {
            runCatching { repository.searchJobs(filter) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            urgentJobs = result.urgent,
                            recommendedJobs = result.recommended,
                            filter = filter,
                        )
                    }
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

    fun setViewMode(mode: JobViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun openFilterSheet() = _uiState.update { it.copy(isFilterSheetOpen = true) }

    fun dismissFilterSheet() = _uiState.update { it.copy(isFilterSheetOpen = false) }

    fun applyFilter(filter: JobFilter) {
        _uiState.update { it.copy(isFilterSheetOpen = false) }
        search(filter.copy(latitude = _uiState.value.filter.latitude, longitude = _uiState.value.filter.longitude))
    }

    fun retry() = search(_uiState.value.filter)

    /** 즉시 지원 (UI spec 3-2: 성공 시 지원 완료 상태). */
    fun applyTo(jobId: String) {
        viewModelScope.launch {
            try {
                applications.apply(jobId)
                _uiState.update { state ->
                    state.copy(
                        urgentJobs = state.urgentJobs.markApplied(jobId),
                        recommendedJobs = state.recommendedJobs.markApplied(jobId),
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = backend.message(error)) }
            }
        }
    }

    private fun List<WorkerJobPosting>.markApplied(jobId: String) =
        map { if (it.id == jobId) it.copy(applyStatus = ApplyStatus.APPLIED) else it }
}
