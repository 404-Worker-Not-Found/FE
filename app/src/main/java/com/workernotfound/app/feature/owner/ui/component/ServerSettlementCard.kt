package com.workernotfound.app.feature.owner.ui.component

import com.workernotfound.app.core.ui.KoreanDateTime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.feature.owner.domain.model.ServerSettlement
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
internal fun ServerSettlementCard(settlement: ServerSettlement?) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            if (settlement == null) {
                Text(stringResource(R.string.settlement_preparing), color = AppColors.TextSub)
                return@Column
            }
            Text(stringResource(R.string.settlement_server_amount), style = MaterialTheme.typography.labelMedium)
            Text(formatSettlementAmount(settlement.amount, settlement.currency), style = MaterialTheme.typography.titleLarge)
            OwnerInfoLine(stringResource(R.string.settlement_server_status), stringResource(settlementStatusResource(settlement.status)))
            OwnerInfoLine(stringResource(R.string.settlement_paid_at), settlement.paidAt?.let(KoreanDateTime::dateTime)
                ?: stringResource(R.string.settlement_not_paid_yet))
            settlement.payableAt?.let { OwnerInfoLine(stringResource(R.string.settlement_payable_at), KoreanDateTime.dateTime(it)) }
            if (settlement.payoutReviewRequired) Text(stringResource(R.string.settlement_review_required), color = AppColors.Warning)
            when (settlement.holdReason) {
                "FUNDING_BLOCKED" -> Text(stringResource(R.string.settlement_funding_blocked), color = AppColors.Warning)
                "WORK_TERMINATED" -> Text(stringResource(R.string.settlement_work_terminated), color = AppColors.Warning)
            }
            Text(stringResource(R.string.settlement_read_only), style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
        }
    }
}

internal fun settlementStatusResource(status: String): Int = when (status) {
    "RESERVED" -> R.string.settlement_reserved
    "ON_HOLD" -> R.string.settlement_on_hold
    "PAYOUT_REQUESTED" -> R.string.settlement_requested
    "PAYOUT_FAILED" -> R.string.settlement_failed
    "PAID" -> R.string.settlement_paid
    else -> R.string.settlement_unknown
}

internal fun formatSettlementAmount(amount: BigDecimal, currencyCode: String): String {
    val currency = try { Currency.getInstance(currencyCode) } catch (_: IllegalArgumentException) { null }
    if (currency == null) return "${amount.toPlainString()} $currencyCode"
    return NumberFormat.getCurrencyInstance(Locale.KOREA).apply {
        this.currency = currency
        minimumFractionDigits = 0
        maximumFractionDigits = amount.stripTrailingZeros().scale().coerceAtLeast(0)
    }.format(amount)
}
