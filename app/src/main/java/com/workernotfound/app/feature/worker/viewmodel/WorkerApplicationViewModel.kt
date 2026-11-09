package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerApplicationViewModel @Inject constructor(
    private val backend: BackendAccess,
    private val repository: WorkerApplicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerApplicationUiState())
    val uiState: StateFlow<WorkerApplicationUiState> = _uiState.asStateFlow()

    private var loading: kotlinx.coroutines.Job? = null
    private var cancelling = false

    init {
        load()
    }

    fun load() = refresh(silent = false)
    fun poll() = refresh(silent = true)
    private fun refresh(silent: Boolean) {
        if (loading?.isActive == true || cancelling) return
        _uiState.update { it.copy(isLoading = if (silent) it.isLoading else true, errorMessage = null) }
        loading = viewModelScope.launch {
            runCatching { repository.getApplications() }
                .onSuccess { apps ->
                    _uiState.update { it.copy(isLoading = false, applications = apps) }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = backend.message(t))
                    }
                }
        }
    }

    fun requestCancel(id: String) = _uiState.update { it.copy(pendingCancelId = id) }

    fun dismissCancel() = _uiState.update { it.copy(pendingCancelId = null) }

    /** 지원 취소: 목록에서 제거하지 않고 상태만 CANCELED로 표시 (communication diagram: setCanceled). */
    fun confirmCancel() {
        val id = _uiState.value.pendingCancelId ?: return
        if (cancelling) return
        cancelling = true
        loading?.cancel()
        viewModelScope.launch {
            try {
                repository.cancel(id)
                _uiState.update { state -> state.copy(
                    applications = state.applications.map { if (it.id == id) it.copy(status = ApplicationStatus.CANCELED) else it },
                    pendingCancelId = null, isLoading = false,
                ) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(pendingCancelId = null, isLoading = false, errorMessage = backend.message(error)) }
            } finally { cancelling = false }
        }
    }
}
