package com.workernotfound.app.feature.owner.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.owner.domain.model.RematchResult
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerRematchViewModel @Inject constructor(
    private val repository: OwnerWorkRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val workId: String = checkNotNull(savedStateHandle[ARG_WORK_ID]) {
        "workId argument is required for OwnerRematchScreen"
    }

    private val _uiState = MutableStateFlow(OwnerRematchUiState())
    val uiState: StateFlow<OwnerRematchUiState> = _uiState.asStateFlow()

    init {
        startRematch()
    }

    /** 자동 재매칭 안내 → 재매칭 성공 / 실패 (UI spec 2-5). */
    fun startRematch() {
        _uiState.update { it.copy(phase = RematchPhase.SEARCHING, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                val noShowWork = repository.getWork(workId)
                _uiState.update { it.copy(noShowWork = noShowWork) }
                repository.requestRematch(workId)
            }
                .onSuccess { result -> applyResult(result) }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(phase = RematchPhase.ERROR, errorMessage = t.message ?: "재매칭을 진행하지 못했습니다.")
                    }
                }
        }
    }

    /** 재매칭 실패 → 공고 재오픈. */
    fun reopenPosting() = resolveFailure(RematchPhase.REOPENED) { repository.reopenPosting(workId) }

    /** 재매칭 실패 → 마감. */
    fun closePosting() = resolveFailure(RematchPhase.CLOSED) { repository.closePosting(workId) }

    private fun applyResult(result: RematchResult) {
        val now = System.currentTimeMillis()
        _uiState.update {
            when (result) {
                is RematchResult.Success -> it.copy(
                    phase = RematchPhase.SUCCESS,
                    newWork = result.newWork,
                    newApplicant = result.applicant,
                    resolvedAtMillis = now,
                )
                RematchResult.Failure -> it.copy(phase = RematchPhase.FAILURE, resolvedAtMillis = now)
            }
        }
    }

    private fun resolveFailure(target: RematchPhase, action: suspend () -> Unit) {
        val state = _uiState.value
        if (state.phase != RematchPhase.FAILURE || state.isProcessing) return
        _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { action() }
                .onSuccess { _uiState.update { it.copy(isProcessing = false, phase = target) } }
                .onFailure { t ->
                    _uiState.update { it.copy(isProcessing = false, errorMessage = t.message ?: "처리하지 못했습니다.") }
                }
        }
    }

    companion object {
        const val ARG_WORK_ID = "workId"
    }
}
