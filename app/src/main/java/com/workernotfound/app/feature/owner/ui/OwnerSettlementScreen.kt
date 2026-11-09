package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.workernotfound.app.R
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.feature.owner.ui.component.ServerSettlementCard
import com.workernotfound.app.feature.owner.ui.component.formatSettlementAmount
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.ui.component.OwnerDialog
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.viewmodel.OwnerSettlementUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerSettlementViewModel

/** 정산 (UI spec 2-4): 서버 정산 금액 + 정산 확인. */
@Composable
fun OwnerSettlementScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerSettlementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OwnerSettlementContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onApprove = viewModel::requestApprove,
        modifier = modifier,
    )
    SettlementDialogs(
        uiState = uiState,
        onConfirm = viewModel::approve,
        onDismiss = viewModel::dismissDialogs,
        onDone = onDone,
    )
}

@Composable
private fun OwnerSettlementContent(
    uiState: OwnerSettlementUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "정산", onBack = onBack)
        val work = uiState.work
        when {
            uiState.isLoading -> OwnerLoadingBox()
            work == null -> OwnerMessageBox(
                message = uiState.errorMessage ?: "정산 내역을 불러오지 못했어요.",
                actionText = "다시 시도",
                onAction = onRetry,
            )
            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(AppDimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
                ) {
                    WorkSummaryCard(work)
                    ServerSettlementCard(uiState.serverSettlement)
                }
                AppPrimaryButton(
                    text = if (uiState.isApproved) "지급 승인 완료" else "정산 확인",
                    onClick = onApprove,
                    enabled = uiState.canApprove,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(AppDimens.screenPadding),
                )
            }
        }
    }
}

@Composable
private fun WorkSummaryCard(work: OwnerWork) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = work.workerName, style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
        Spacer(Modifier.height(2.dp))
        Text(text = work.workSummary, color = AppColors.TextSub, fontSize = 13.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            text = "${OwnerFormat.date(work.scheduledStartMillis)} " +
                "${OwnerFormat.clock(work.scheduledStartMillis)} ~ ${OwnerFormat.clock(work.scheduledEndMillis)}",
            color = AppColors.TextSub,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SettlementDialogs(
    uiState: OwnerSettlementUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
) {
    val total = uiState.serverSettlement?.let { formatSettlementAmount(it.amount, it.currency) }.orEmpty()
    when {
        uiState.isConfirmDialogVisible -> OwnerDialog(
            title = "정산 확인",
            message = "최종 지급액 ${total}을 지급 승인할까요?",
            confirmText = "지급 승인",
            onConfirm = onConfirm,
            dismissText = "취소",
            onDismiss = onDismiss,
        )
        uiState.actionErrorMessage != null -> OwnerDialog(
            title = "지급 승인 실패",
            message = uiState.actionErrorMessage,
            confirmText = "확인",
            onConfirm = onDismiss,
        )
        uiState.isApproved -> OwnerDialog(
            title = "지급 승인 완료",
            message = "${uiState.work?.workerName.orEmpty()} 님에게 $total 지급을 승인했어요.",
            confirmText = "확인",
            onConfirm = onDone,
        )
    }
}

