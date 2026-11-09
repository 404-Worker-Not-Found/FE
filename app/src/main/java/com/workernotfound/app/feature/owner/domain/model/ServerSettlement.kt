package com.workernotfound.app.feature.owner.domain.model

import java.math.BigDecimal

/** Server-owned payout record. Amounts are never reconstructed on the client. */
data class ServerSettlement(
    val id: Long,
    val workId: Long,
    val jobPostId: Long,
    val amount: BigDecimal,
    val currency: String,
    val status: String,
    val holdReason: String?,
    val payoutReviewRequired: Boolean,
    val workCompletedAt: String?,
    val payableAt: String?,
    val paidAt: String?,
)
