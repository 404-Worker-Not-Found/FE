package com.workernotfound.app.feature.owner.domain.model

/** A matched shift the owner monitors (UI spec 2-4: 근무 현황). */
data class OwnerWork(
    val id: String,
    val postingId: String,
    val workSummary: String,
    val workerId: String,
    val workerName: String,
    val scheduledStartMillis: Long,
    val scheduledEndMillis: Long,
    val hourlyWage: Int,
    val bonusPerHour: Int,
    val isAttendanceVerified: Boolean,
    val workerDistanceMeters: Int,
    val status: WorkProgressStatus,
)

/**
 * ACTIVE: matched, before or during the shift. AWAITING_SETTLEMENT: 근무 완료 pressed,
 * settlement not yet approved. SETTLED / NO_SHOW: closed out.
 */
enum class WorkProgressStatus { ACTIVE, AWAITING_SETTLEMENT, SETTLED, NO_SHOW }

/** Settlement breakdown (UI spec 2-4: 총 근무시간, 시급, 가산액, 최종 지급액). */
data class Settlement(
    val workedMinutes: Long,
    val hourlyWage: Int,
    val basePay: Int,
    val bonusAmount: Int,
) {
    val totalPay: Int get() = basePay + bonusAmount
}

/** Result of the automatic re-matching after a no-show (UI spec 2-5). */
sealed interface RematchResult {
    data class Success(val newWork: OwnerWork, val applicant: Applicant) : RematchResult
    data object Failure : RematchResult
}
