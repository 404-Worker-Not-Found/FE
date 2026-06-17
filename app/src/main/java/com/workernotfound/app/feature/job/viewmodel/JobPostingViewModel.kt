package com.workernotfound.app.feature.job.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.job.domain.model.JobCategory
import com.workernotfound.app.feature.job.domain.model.JobPostingDraft
import com.workernotfound.app.feature.job.domain.model.RecruitRadius
import com.workernotfound.app.feature.job.domain.model.WorkDay
import com.workernotfound.app.feature.job.domain.repository.JobPostingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobPostingViewModel @Inject constructor(
    private val repository: JobPostingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobPostingUiState())
    val uiState: StateFlow<JobPostingUiState> = _uiState.asStateFlow()

    fun onWorkDayChange(workDay: WorkDay) = _uiState.update { it.copy(workDay = workDay) }

    fun onStartTimeChange(time: String) = _uiState.update { it.copy(startTime = time) }

    fun onEndTimeChange(time: String) = _uiState.update { it.copy(endTime = time) }

    fun onDescriptionChange(text: String) = _uiState.update {
        it.copy(description = text.take(DESCRIPTION_MAX_LENGTH))
    }

    fun onBaseWageChange(text: String) = _uiState.update {
        it.copy(baseWageInput = text.filter(Char::isDigit).take(7))
    }

    fun onRecruitCountChange(count: Int) = _uiState.update { it.copy(recruitCount = count) }

    fun onRadiusChange(radius: RecruitRadius) = _uiState.update { it.copy(radius = radius) }

    fun onCategoryChange(category: JobCategory) = _uiState.update { it.copy(category = category) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.createPosting(state.toDraft())
            result
                .onSuccess { _uiState.update { s -> s.copy(isSubmitting = false, showSuccessDialog = true) } }
                .onFailure { throwable ->
                    _uiState.update { s ->
                        s.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "공고 등록에 실패했습니다. 다시 시도해 주세요.",
                        )
                    }
                }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    private fun JobPostingUiState.toDraft() = JobPostingDraft(
        workDay = requireNotNull(workDay),
        startTime = requireNotNull(startTime),
        endTime = requireNotNull(endTime),
        description = description,
        baseWage = baseWage,
        recruitCount = recruitCount,
        radius = requireNotNull(radius),
        category = requireNotNull(category),
        address = address,
    )
}
