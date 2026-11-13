package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.location.AddressResolver
import com.workernotfound.app.core.location.DeviceLocationProvider
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobFeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerJobSearchViewModel @Inject constructor(
    private val applications: WorkerApplicationRepository,
    private val backend: BackendAccess,
    private val feed: WorkerJobFeedRepository,
    private val positions: DeviceLocationProvider,
    private val addresses: AddressResolver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerJobSearchUiState())
    val uiState: StateFlow<WorkerJobSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var locationJob: Job? = null

    init {
        search(JobFilter())
    }

    fun onQueryChange(text: String) = _uiState.update { it.copy(query = text) }

    /** 동네 · 가게 검색: store/category names first; otherwise the text is resolved as a place. */
    fun submitQuery() {
        val text = _uiState.value.query.trim()
        if (text.isEmpty()) { clearQuery(); return }
        val state = _uiState.value
        // An earlier area search re-centred the query; return to the user's own origin first.
        val base = if (state.searchMode == SearchMode.AREA) state.filter.copy(latitude = null, longitude = null) else state.filter
        if (state.searchMode != SearchMode.AREA && state.jobs.any { it.matchesText(text) }) {
            _uiState.update { it.copy(submittedQuery = text, searchMode = SearchMode.STORE) }
            return
        }
        searchJob?.cancel()
        _uiState.update { it.copy(isLoading = true, isRefreshing = false, errorMessage = null, submittedQuery = text) }
        searchJob = viewModelScope.launch {
            try {
                val area = if (addresses.available()) addresses.find(text).firstOrNull() else null
                if (area == null) {
                    val result = feed.load(base)
                    val mode = if (result.jobs.any { it.matchesText(text) }) SearchMode.STORE else SearchMode.NO_RESULT
                    applyResult(result, base, mode)
                } else {
                    val centred = base.copy(latitude = area.latitude, longitude = area.longitude)
                    applyResult(feed.load(centred), centred, SearchMode.AREA)
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(error)) }
            }
        }
    }

    fun clearQuery() {
        val state = _uiState.value
        _uiState.update { it.copy(query = "", submittedQuery = "", searchMode = SearchMode.NONE) }
        if (state.searchMode == SearchMode.AREA) search(state.filter.copy(latitude = null, longitude = null))
    }

    /** Called after the location permission was granted from the filter sheet or the map tab. */
    fun locate() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            _uiState.update { it.copy(locating = true, locationError = false) }
            try {
                val point = positions.current()
                _uiState.update { it.copy(locating = false, searchMode = SearchMode.NONE, query = "", submittedQuery = "") }
                search(_uiState.value.filter.copy(latitude = point.latitude, longitude = point.longitude))
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { locationDenied() }
        }
    }

    fun locationDenied() { _uiState.update { it.copy(locating = false, locationError = true) } }

    /** Back to the default origin (last known device location, then the registered base location). */
    fun useDefaultLocation() {
        _uiState.update { it.copy(isFilterSheetOpen = false, locationError = false) }
        search(_uiState.value.filter.copy(latitude = null, longitude = null))
    }

    private fun search(filter: JobFilter) {
        searchJob?.cancel()
        // A new search cancels any refresh in flight.
        _uiState.update { it.copy(isLoading = true, isRefreshing = false, errorMessage = null, filter = filter) }
        searchJob = viewModelScope.launch {
            try {
                val keepStore = _uiState.value.searchMode == SearchMode.STORE
                applyResult(feed.load(filter), filter, if (keepStore) SearchMode.STORE else SearchMode.NONE)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(error)) }
            }
        }
    }

    /**
     * Reload when the tab is shown again or pulled: same filter, same search text/area, list stays visible.
     * Skipped while another search is still running.
     */
    fun refresh() {
        if (searchJob?.isActive == true) return
        val state = _uiState.value
        _uiState.update { it.copy(isRefreshing = true) }
        searchJob = viewModelScope.launch {
            try {
                applyResult(feed.load(state.filter), state.filter, state.searchMode)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isRefreshing = false, errorMessage = backend.message(error)) }
            }
        }
    }

    private fun applyResult(
        result: com.workernotfound.app.feature.worker.domain.repository.WorkerJobFeed,
        filter: JobFilter,
        mode: SearchMode,
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = null,
                jobs = result.jobs,
                preference = result.preference,
                origin = result.origin,
                originLatitude = result.latitude,
                originLongitude = result.longitude,
                filter = filter,
                searchMode = mode,
            )
        }
    }

    fun setViewMode(mode: JobViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun openFilterSheet() = _uiState.update { it.copy(isFilterSheetOpen = true) }

    fun dismissFilterSheet() = _uiState.update { it.copy(isFilterSheetOpen = false) }

    fun applyFilter(filter: JobFilter) {
        _uiState.update { it.copy(isFilterSheetOpen = false) }
        val current = _uiState.value.filter
        search(filter.copy(latitude = current.latitude, longitude = current.longitude))
    }

    fun retry() = search(_uiState.value.filter)

    /** 즉시 지원 (UI spec 3-2: 성공 시 지원 완료 상태). */
    fun applyTo(jobId: String) {
        viewModelScope.launch {
            try {
                applications.apply(jobId)
                _uiState.update { state ->
                    state.copy(jobs = state.jobs.map { if (it.id == jobId) it.copy(applyStatus = ApplyStatus.APPLIED) else it })
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = backend.message(error)) }
            }
        }
    }
}
