package com.workernotfound.app.feature.worker.domain.model

/**
 * A worker's application to a job posting (UI spec 3-4 지원 현황 / 3-5 매칭 결과).
 * One model carries both the list summary (3-4) and the matched detail (3-5)
 * so navigating from the status list to the match result reuses it.
 */
data class WorkerApplication(
    val id: String,
    val storeName: String,
    val category: String,
    val appliedAtText: String,
    val status: ApplicationStatus,
    val workDate: String,
    val workTimeRange: String,
    val address: String,
    val hourlyWage: Int,
    val matchScore: Int,
    /** NOT_SELECTED 지원자에게 노쇼 재매칭 기회가 온 경우 (UI spec 3-5 재매칭 대기). */
    val hasRematchOffer: Boolean = false,
    /** 재매칭 제안 시 현재 위치 기준 도착 예상 (분). */
    val etaMinutes: Int = 0,
) {
    /** 매칭 확정 전(지원중/매칭중)에만 지원 취소 가능 (UI spec 3-4). */
    val canCancel: Boolean
        get() = status == ApplicationStatus.APPLYING || status == ApplicationStatus.MATCHING
}

/** 지원 상태 (UI spec 3-4: 지원중 / 매칭중 / 매칭완료 / 미선정, + 지원 취소). */
enum class ApplicationStatus { APPLYING, MATCHING, MATCHED, NOT_SELECTED, CANCELED }
