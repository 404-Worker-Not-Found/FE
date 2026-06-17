package com.workernotfound.app.feature.job.viewmodel

import com.workernotfound.app.feature.job.domain.WageBreakdown
import com.workernotfound.app.feature.job.domain.WageCalculator
import com.workernotfound.app.feature.job.domain.model.JobCategory
import com.workernotfound.app.feature.job.domain.model.MIN_HOURLY_WAGE
import com.workernotfound.app.feature.job.domain.model.RecruitRadius
import com.workernotfound.app.feature.job.domain.model.WorkDay

/** Immutable UI state for the job-posting form (UI spec 2-2). */
data class JobPostingUiState(
    val workDay: WorkDay? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String = "",
    val baseWageInput: String = "",
    val recruitCount: Int = 1,
    val radius: RecruitRadius? = null,
    val category: JobCategory? = null,
    val address: String = "서울특별시 강남구 테헤란로 123",
    val isSubmitting: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val errorMessage: String? = null,
) {
    val baseWage: Int get() = baseWageInput.toIntOrNull() ?: 0

    /** false only when a wage has been entered and it is below the minimum. */
    val isWageBelowMinimum: Boolean get() = baseWageInput.isNotEmpty() && baseWage < MIN_HOURLY_WAGE

    val wage: WageBreakdown get() = WageCalculator.calculate(baseWage, startTime)

    val isTimeRangeValid: Boolean
        get() {
            val start = startTime ?: return true
            val end = endTime ?: return true
            return end > start
        }

    val canSubmit: Boolean
        get() = workDay != null &&
            startTime != null &&
            endTime != null &&
            isTimeRangeValid &&
            description.isNotBlank() &&
            baseWage >= MIN_HOURLY_WAGE &&
            radius != null &&
            category != null &&
            !isSubmitting
}

const val DESCRIPTION_MAX_LENGTH = 200
