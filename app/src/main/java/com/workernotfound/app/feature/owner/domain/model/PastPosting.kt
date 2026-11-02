package com.workernotfound.app.feature.owner.domain.model

/** A finished posting (UI spec 2-6: 지난 공고 — 날짜, 업무, 매칭된 알바생, 상태). */
data class PastPosting(
    val postingId: String,
    val workSummary: String,
    val workerName: String,
    val workStartMillis: Long,
    val workEndMillis: Long,
    val hourlyWage: Int,
    val status: PastPostingStatus,
    val settlement: SettlementRecord?,
    val workId: String? = null,
    val serverSettlement: ServerSettlement? = null,
) {
    val entryId: String get() = workId?.let { "work-$it" } ?: postingId
}

/** UI spec 2-6: 완료 / 노쇼 / 분쟁. */
enum class PastPostingStatus { COMPLETED, NO_SHOW, DISPUTE, CANCELED, FAILED, CLOSED }

/** Final settlement of a past posting. [paidAtMillis] is null while payment is on hold. */
data class SettlementRecord(
    val totalPay: Int,
    val paidAtMillis: Long?,
)
