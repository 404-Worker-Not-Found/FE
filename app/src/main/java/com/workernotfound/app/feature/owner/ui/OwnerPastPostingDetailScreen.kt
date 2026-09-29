package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.core.designsystem.component.StatusBadge
import com.workernotfound.app.feature.owner.domain.ReviewPolicy
import com.workernotfound.app.feature.owner.domain.model.PastPosting
import com.workernotfound.app.feature.owner.domain.model.PastPostingStatus
import com.workernotfound.app.feature.owner.ui.component.OwnerInfoLine
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.viewmodel.OwnerPastPostingDetailUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerPastPostingDetailViewModel

/** 지난 공고 상세 (UI spec 2-6): 리뷰 작성 버튼 + 정산 내역 확인. */
@Composable
fun OwnerPastPostingDetailScreen(
    onBack: () -> Unit,
    onWriteReview: (postingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerPastPostingDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OwnerPastPostingDetailContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onWriteReview = onWriteReview,
        modifier = modifier,
    )
}

@Composable
private fun OwnerPastPostingDetailContent(
    uiState: OwnerPastPostingDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onWriteReview: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "지난 공고 상세", onBack = onBack)
        val posting = uiState.posting
        when {
            uiState.isLoading -> OwnerLoadingBox()
            posting == null -> OwnerMessageBox(
                message = uiState.errorMessage ?: "지난 공고를 불러오지 못했어요.",
                actionText = "다시 시도",
                onAction = onRetry,
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppDimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
            ) {
                SummaryCard(posting)
                SettlementSection(posting)
                ReviewSection(
                    posting = posting,
                    canWriteReview = uiState.canWriteReview,
                    nowMillis = uiState.nowMillis,
                    onWriteReview = { onWriteReview(posting.postingId) },
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(posting: PastPosting) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = OwnerFormat.date(posting.workStartMillis),
                color = AppColors.TextSub,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )
            StatusBadge(status = posting.status.toBadge())
        }
        Spacer(Modifier.height(8.dp))
        Text(text = posting.workSummary, style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
        Spacer(Modifier.height(12.dp))
        OwnerInfoLine(label = "매칭 알바생", value = posting.workerName)
        Spacer(Modifier.height(8.dp))
        OwnerInfoLine(
            label = "근무 시간",
            value = "${OwnerFormat.clock(posting.workStartMillis)} ~ ${OwnerFormat.clock(posting.workEndMillis)}",
        )
        Spacer(Modifier.height(8.dp))
        OwnerInfoLine(label = "시급", value = OwnerFormat.won(posting.hourlyWage))
    }
}

/** 정산 내역 확인: 최종 정산 금액 및 지급 일시. */
@Composable
private fun SettlementSection(posting: PastPosting) {
    val settlement = posting.settlement
    Column {
        SectionLabel(text = "정산 내역")
        Spacer(Modifier.height(8.dp))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            if (settlement == null) {
                Text(text = noSettlementText(posting.status), color = AppColors.TextSub, fontSize = 13.sp)
                return@AppCard
            }
            OwnerInfoLine(
                label = "최종 정산 금액",
                value = OwnerFormat.won(settlement.totalPay),
                valueColor = AppTheme.role.accent,
                isEmphasized = true,
            )
            Spacer(Modifier.height(10.dp))
            OwnerInfoLine(
                label = "지급 일시",
                value = settlement.paidAtMillis?.let(OwnerFormat::dateTime) ?: "지급 보류 (분쟁 처리 중)",
                valueColor = if (settlement.paidAtMillis == null) AppColors.Warning else AppColors.TextMain,
            )
        }
    }
}

private fun noSettlementText(status: PastPostingStatus): String = when (status) {
    PastPostingStatus.NO_SHOW -> "노쇼로 처리되어 지급된 금액이 없어요."
    else -> "정산 내역이 없어요."
}

/** 리뷰 작성 버튼: 근무 종료 후 7일 이내 활성화. */
@Composable
private fun ReviewSection(
    posting: PastPosting,
    canWriteReview: Boolean,
    nowMillis: Long,
    onWriteReview: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        AppPrimaryButton(text = "리뷰 작성", onClick = onWriteReview, enabled = canWriteReview)
        Spacer(Modifier.height(6.dp))
        Text(
            text = reviewHint(posting, canWriteReview, nowMillis),
            color = AppColors.TextSub,
            fontSize = 12.sp,
        )
    }
}

private fun reviewHint(posting: PastPosting, canWriteReview: Boolean, nowMillis: Long): String =
    if (canWriteReview) {
        "리뷰 작성 기간이 ${ReviewPolicy.remainingDays(posting.workEndMillis, nowMillis)}일 남았어요."
    } else {
        "리뷰는 근무 종료 후 ${ReviewPolicy.REVIEW_WINDOW_DAYS}일 이내에만 작성할 수 있어요."
    }
