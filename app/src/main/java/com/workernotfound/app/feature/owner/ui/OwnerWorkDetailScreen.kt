package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.owner.domain.NoShowPolicy
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import com.workernotfound.app.feature.owner.ui.component.AttendanceBadge
import com.workernotfound.app.feature.owner.ui.component.OwnerDialog
import com.workernotfound.app.feature.owner.ui.component.OwnerLiveLocationMap
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.ui.component.OwnerOutlinedButton
import com.workernotfound.app.feature.owner.ui.component.WorkProgressBadge
import com.workernotfound.app.feature.owner.viewmodel.OwnerWorkDetailEvent
import com.workernotfound.app.feature.owner.viewmodel.OwnerWorkDetailUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerWorkDetailViewModel
import com.workernotfound.app.feature.owner.viewmodel.WorkTiming

/** 근무 현황 (UI spec 2-4): 출근 확인, 실시간 위치, 타이머, 노쇼 확정, 근무 완료. */
@Composable
fun OwnerWorkDetailScreen(
    onBack: () -> Unit,
    onOpenSettlement: (workId: String) -> Unit,
    onNoShowConfirmed: (workId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerWorkDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val openSettlement by rememberUpdatedState(onOpenSettlement)
    val noShowConfirmed by rememberUpdatedState(onNoShowConfirmed)
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is OwnerWorkDetailEvent.OpenSettlement -> openSettlement(event.workId)
                is OwnerWorkDetailEvent.NoShowConfirmed -> noShowConfirmed(event.workId)
            }
        }
    }
    OwnerWorkDetailContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onNoShow = viewModel::requestNoShow,
        onComplete = viewModel::requestComplete,
        onOpenSettlement = viewModel::openSettlement,
        modifier = modifier,
    )
    WorkDetailDialogs(
        uiState = uiState,
        onConfirmNoShow = viewModel::confirmNoShow,
        onConfirmComplete = viewModel::completeWork,
        onDismiss = viewModel::dismissDialogs,
    )
}

@Composable
private fun OwnerWorkDetailContent(
    uiState: OwnerWorkDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onNoShow: () -> Unit,
    onComplete: () -> Unit,
    onOpenSettlement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "근무 현황", onBack = onBack)
        val timing = uiState.timing
        when {
            uiState.isLoading -> OwnerLoadingBox()
            timing == null -> OwnerMessageBox(
                message = uiState.errorMessage ?: "근무 정보를 불러오지 못했어요.",
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
                    WorkHeader(timing)
                    AttendanceSection(timing)
                    TimerSection(timing)
                }
                ActionBar(
                    timing = timing,
                    isProcessing = uiState.isProcessing,
                    onNoShow = onNoShow,
                    onComplete = onComplete,
                    onOpenSettlement = onOpenSettlement,
                )
            }
        }
    }
}

@Composable
private fun WorkHeader(timing: WorkTiming) {
    val work = timing.work
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WorkProgressBadge(timing)
            Spacer(Modifier.weight(1f))
            Text(
                text = "시급 ${OwnerFormat.won(work.hourlyWage)}",
                color = AppTheme.role.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(text = work.workerName, style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
        Spacer(Modifier.height(2.dp))
        Text(text = work.workSummary, color = AppColors.TextSub, fontSize = 13.sp)
    }
}

@Composable
private fun AttendanceSection(timing: WorkTiming) {
    val work = timing.work
    Column {
        SectionLabel(text = "출근 확인 · 실시간 위치")
        Spacer(Modifier.height(8.dp))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "GPS 출근 인증", color = AppColors.TextMain, fontSize = 14.sp, modifier = Modifier.weight(1f))
                AttendanceBadge(isVerified = work.isAttendanceVerified)
            }
            Spacer(Modifier.height(12.dp))
            OwnerLiveLocationMap(distanceMeters = work.workerDistanceMeters)
        }
    }
}

@Composable
private fun TimerSection(timing: WorkTiming) {
    val work = timing.work
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = "근무 진행 타이머", color = AppColors.TextSub, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = timerText(timing),
            color = if (timing.isActive && timing.hasStarted) AppTheme.role.accent else AppColors.TextSub,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "시작 ${OwnerFormat.clock(work.scheduledStartMillis)} · 종료 예정 ${OwnerFormat.clock(work.scheduledEndMillis)}",
            color = AppColors.TextSub,
            fontSize = 12.sp,
        )
    }
}

private fun timerText(timing: WorkTiming): String = when {
    timing.work.status == WorkProgressStatus.AWAITING_SETTLEMENT -> "근무 종료"
    timing.work.status == WorkProgressStatus.NO_SHOW -> "노쇼 처리됨"
    !timing.hasStarted -> "시작 전"
    else -> OwnerFormat.elapsed(timing.elapsedMillis)
}

@Composable
private fun ActionBar(
    timing: WorkTiming,
    isProcessing: Boolean,
    onNoShow: () -> Unit,
    onComplete: () -> Unit,
    onOpenSettlement: () -> Unit,
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(AppDimens.screenPadding),
    ) {
        if (timing.work.status == WorkProgressStatus.AWAITING_SETTLEMENT) {
            AppPrimaryButton(text = "정산 확인하기", onClick = onOpenSettlement)
            return@Column
        }
        if (!timing.isActive) return@Column
        Text(text = noShowHint(timing), color = AppColors.TextSub, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row {
            OwnerOutlinedButton(
                text = "노쇼 확정",
                onClick = onNoShow,
                enabled = timing.canConfirmNoShow && !isProcessing,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            AppPrimaryButton(
                text = "근무 완료",
                onClick = onComplete,
                enabled = timing.canCompleteWork && !isProcessing,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun noShowHint(timing: WorkTiming): String = when {
    timing.work.isAttendanceVerified -> "출근 인증이 완료되어 근무 종료 후 정산할 수 있어요."
    timing.canConfirmNoShow -> "근무 시작 ${NoShowPolicy.GRACE_MINUTES}분이 지났지만 출근 인증이 없어요."
    else -> "노쇼 확정은 근무 시작 ${NoShowPolicy.GRACE_MINUTES}분 후 활성화돼요 " +
        "(${OwnerFormat.countdown(timing.noShowUnlockRemainingMillis)} 남음)."
}

@Composable
private fun WorkDetailDialogs(
    uiState: OwnerWorkDetailUiState,
    onConfirmNoShow: () -> Unit,
    onConfirmComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val name = uiState.work?.workerName.orEmpty()
    when {
        uiState.isNoShowDialogVisible -> OwnerDialog(
            title = "노쇼 확정",
            message = "$name 님을 노쇼로 확정할까요?\n확정하면 자동으로 재매칭을 진행해요.",
            confirmText = "확인",
            onConfirm = onConfirmNoShow,
            dismissText = "취소",
            onDismiss = onDismiss,
            isDestructive = true,
        )
        uiState.isCompleteDialogVisible -> OwnerDialog(
            title = "근무 완료",
            message = "$name 님의 근무를 종료하고 정산을 진행할까요?",
            confirmText = "근무 완료",
            onConfirm = onConfirmComplete,
            dismissText = "취소",
            onDismiss = onDismiss,
        )
        uiState.actionErrorMessage != null -> OwnerDialog(
            title = "처리 실패",
            message = uiState.actionErrorMessage,
            confirmText = "확인",
            onConfirm = onDismiss,
        )
    }
}
