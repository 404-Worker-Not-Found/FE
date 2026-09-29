package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.Applicant
import com.workernotfound.app.feature.owner.domain.model.OwnerWork

/** 노쇼 처리 → 자동 재매칭 진행 단계 (UI spec 2-5). */
enum class RematchPhase { SEARCHING, SUCCESS, FAILURE, REOPENED, CLOSED, ERROR }

/** 노쇼 처리 / 재매칭 결과 상태 (UI spec 2-5). */
data class OwnerRematchUiState(
    val phase: RematchPhase = RematchPhase.SEARCHING,
    val noShowWork: OwnerWork? = null,
    val newWork: OwnerWork? = null,
    val newApplicant: Applicant? = null,
    /** When the re-match resolved; the arrival time is this + ETA. */
    val resolvedAtMillis: Long = 0L,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
) {
    val expectedArrivalMillis: Long?
        get() = newApplicant?.let { resolvedAtMillis + it.etaMinutes * 60_000L }
}
