package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import com.workernotfound.app.feature.owner.ui.component.AttendanceBadge
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.ui.component.WorkProgressBadge
import com.workernotfound.app.feature.owner.viewmodel.OwnerWorkListUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerWorkListViewModel
import com.workernotfound.app.feature.owner.viewmodel.WorkTiming

/** 근무관리 탭 (UI spec 2-4): 진행 중 / 정산 대기 근무 목록. */
@Composable
fun OwnerWorkListScreen(
    onWorkClick: (workId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerWorkListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    OwnerWorkListContent(
        uiState = uiState,
        onWorkClick = onWorkClick,
        onRetry = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
private fun OwnerWorkListContent(
    uiState: OwnerWorkListUiState,
    onWorkClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "근무 관리",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
            modifier = Modifier.padding(
                start = AppDimens.screenPadding,
                end = AppDimens.screenPadding,
                top = 20.dp,
                bottom = 8.dp,
            ),
        )
        when {
            uiState.isLoading -> OwnerLoadingBox()
            uiState.errorMessage != null -> OwnerMessageBox(
                message = uiState.errorMessage,
                actionText = "다시 시도",
                onAction = onRetry,
            )
            uiState.works.isEmpty() -> OwnerMessageBox(message = "진행 중인 근무가 없어요.")
            else -> WorkList(timings = uiState.timings, onWorkClick = onWorkClick)
        }
    }
}

@Composable
private fun WorkList(timings: List<WorkTiming>, onWorkClick: (String) -> Unit) {
    val (awaiting, active) = timings.partition { it.work.status == WorkProgressStatus.AWAITING_SETTLEMENT }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
    ) {
        if (active.isNotEmpty()) {
            item { SectionLabel(text = "근무 현황") }
            items(items = active, key = { it.work.id }) { WorkCard(it, onClick = { onWorkClick(it.work.id) }) }
        }
        if (awaiting.isNotEmpty()) {
            item { SectionLabel(text = "정산 대기") }
            items(items = awaiting, key = { it.work.id }) { WorkCard(it, onClick = { onWorkClick(it.work.id) }) }
        }
    }
}

@Composable
private fun WorkCard(timing: WorkTiming, onClick: () -> Unit) {
    val work = timing.work
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WorkProgressBadge(timing)
            Spacer(Modifier.width(6.dp))
            AttendanceBadge(isVerified = work.isAttendanceVerified)
            Spacer(Modifier.weight(1f))
            Text(
                text = "${OwnerFormat.clock(work.scheduledStartMillis)} ~ ${OwnerFormat.clock(work.scheduledEndMillis)}",
                color = AppColors.TextSub,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(text = work.workerName, style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
        Spacer(Modifier.height(2.dp))
        Text(text = work.workSummary, color = AppColors.TextSub, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = cardHint(timing), color = hintColor(timing), fontSize = 12.sp, modifier = Modifier.weight(1f))
            if (timing.isActive && timing.hasStarted) {
                Text(
                    text = OwnerFormat.elapsed(timing.elapsedMillis),
                    color = AppTheme.role.accent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun cardHint(timing: WorkTiming): String = when {
    timing.work.status == WorkProgressStatus.AWAITING_SETTLEMENT -> "근무 종료 · 정산 확인이 필요해요"
    timing.canConfirmNoShow -> "출근 확인이 안 돼요 · 노쇼 처리 가능"
    !timing.hasStarted -> "${OwnerFormat.clock(timing.work.scheduledStartMillis)} 근무 시작 예정"
    else -> "근무 진행 시간"
}

private fun hintColor(timing: WorkTiming) =
    if (timing.canConfirmNoShow) AppColors.Danger else AppColors.TextSub
