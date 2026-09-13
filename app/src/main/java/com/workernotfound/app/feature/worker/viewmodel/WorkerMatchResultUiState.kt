package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.WorkerApplication

/** 재매칭 제안 처리 결과 (UI spec 3-5: 수락/거절, 시간 초과). */
enum class RematchOutcome { PENDING, ACCEPTED, REJECTED, EXPIRED }

/** 매칭 결과 화면 상태 (UI spec 3-5: 매칭 성공 / 재매칭 대기). */
data class WorkerMatchResultUiState(
    val isLoading: Boolean = true,
    val application: WorkerApplication? = null,
    /** 재매칭 응답 제한시간 카운트다운 (UI spec 3-5: 5분 내 응답). */
    val remainingSeconds: Long = 0,
    val outcome: RematchOutcome = RematchOutcome.PENDING,
    val errorMessage: String? = null,
)
