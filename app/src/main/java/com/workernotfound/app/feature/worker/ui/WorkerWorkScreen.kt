package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppBadge
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.AppStatus
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.core.designsystem.component.StatusBadge
import com.workernotfound.app.core.ui.KoreanDateTime
import com.workernotfound.app.feature.worker.domain.model.WorkSchedule
import com.workernotfound.app.feature.worker.domain.model.WorkStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerApplication
import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
import com.workernotfound.app.feature.worker.viewmodel.WorkerWorkUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerWorkViewModel
import com.workernotfound.app.feature.worker.viewmodel.WorkerApplicationUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerApplicationViewModel
import com.workernotfound.app.core.ui.RefreshWhileResumed
import java.time.Clock

@Composable
fun WorkerWorkScreen(
    onAttendanceClick: () -> Unit,
    onApplicationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerWorkViewModel = hiltViewModel(),
    applicationViewModel: WorkerApplicationViewModel = hiltViewModel(),
    onCancellation: (String) -> Unit = {},
    onMatchClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val applicationState by applicationViewModel.uiState.collectAsStateWithLifecycle()
    RefreshWhileResumed(true, applicationViewModel::poll)
    RefreshWhileResumed(true, viewModel::poll)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.load() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    WorkerWorkContent(
        uiState = uiState, applicationState = applicationState,
        onRetry = viewModel::load, onApplicationsRetry = applicationViewModel::load,
        onRequestCancel = applicationViewModel::requestCancel,
        onMatchClick = onMatchClick, onAttendanceClick = onAttendanceClick,
        onApplicationsClick = onApplicationsClick, modifier = modifier,
        onCancellation = onCancellation,
    )
    if (applicationState.pendingCancelId != null) {
        CancelConfirmDialog(
            onConfirm = applicationViewModel::confirmCancel,
            onDismiss = applicationViewModel::dismissCancel,
        )
    }
}

@Composable
private fun WorkerWorkContent(
    uiState: WorkerWorkUiState,
    applicationState: WorkerApplicationUiState,
    onRetry: () -> Unit,
    onApplicationsRetry: () -> Unit,
    onRequestCancel: (String) -> Unit,
    onMatchClick: (String) -> Unit,
    onAttendanceClick: () -> Unit,
    onApplicationsClick: () -> Unit,
    onCancellation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = Clock.systemUTC().instant()
    val shownApplications = applicationState.applications.filterNot {
        it.status == ApplicationStatus.MATCHED && it.workEndsAt.isAfter(now)
    }
    Column(modifier = modifier.fillMaxSize()) {
        WorkHeader(onApplicationsClick = onApplicationsClick, onAttendanceClick = onAttendanceClick)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AppDimens.screenPadding,
                end = AppDimens.screenPadding,
                bottom = AppDimens.screenPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
        ) {
            when {
                uiState.isLoading -> item { SectionLoading() }
                uiState.errorMessage != null -> item {
                    SectionError(uiState.errorMessage, onRetry)
                }
                else -> {
                    if (uiState.inProgress.isNotEmpty()) {
                        item { SectionLabel(text = stringResource(R.string.worker_work_in_progress)) }
                        items(items = uiState.inProgress, key = { "work-${it.id}" }) { WorkCard(it, onCancellation) }
                    }
                    item { SectionLabel(text = stringResource(R.string.worker_work_scheduled)) }
                    if (uiState.scheduled.isEmpty()) item { SectionEmpty(R.string.worker_work_empty) }
                    items(items = uiState.scheduled, key = { "work-${it.id}" }) { WorkCard(it, onCancellation) }
                }
            }
            item { SectionLabel(text = stringResource(R.string.worker_work_applied)) }
            when {
                applicationState.isLoading -> item { SectionLoading() }
                applicationState.errorMessage != null -> item {
                    SectionError(applicationState.errorMessage, onApplicationsRetry)
                }
                shownApplications.isEmpty() -> item { SectionEmpty(R.string.worker_work_applied_empty) }
                else -> items(items = shownApplications, key = { "application-${it.id}" }) { app ->
                    WorkApplicationCard(
                        app = app,
                        workEnded = !app.workEndsAt.isAfter(now),
                        onMatchClick = { onMatchClick(app.id) },
                        onRequestCancel = { onRequestCancel(app.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkApplicationCard(
    app: WorkerApplication,
    workEnded: Boolean,
    onMatchClick: () -> Unit,
    onRequestCancel: () -> Unit,
) {
    val navigable = app.status == ApplicationStatus.MATCHED || app.hasRematchOffer
    val modifier = Modifier.fillMaxWidth().heightIn(min = 116.dp)
        .then(if (navigable) Modifier.clickable(onClick = onMatchClick) else Modifier)
    AppCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.storeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(2.dp))
                Text(stringResource(R.string.worker_application_meta, app.category, app.workDate),
                    fontSize = 12.sp, color = AppColors.TextSub)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (app.hasRematchOffer) stringResource(R.string.worker_rematch_banner,
                        app.proposalExpiresInSeconds?.let { "${(it + 59) / 60}분" } ?: "1분 미만")
                    else stringResource(R.string.worker_application_meta, app.workTimeRange,
                        stringResource(R.string.worker_application_applied_at, app.appliedAtText)),
                    fontSize = 12.sp,
                    color = if (app.hasRematchOffer) AppColors.Danger else AppColors.TextSub,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                when {
                    app.status == ApplicationStatus.APPLYING -> AppBadge(
                        stringResource(R.string.worker_application_submitted), AppColors.WorkerLight, AppColors.WorkerEmphasis)
                    app.status == ApplicationStatus.MATCHED && workEnded -> AppBadge(
                        stringResource(R.string.worker_application_work_completed), AppColors.SuccessBg, AppColors.Success)
                    else -> StatusBadge(app.status.toBadge())
                }
                if (app.canCancel) {
                    Spacer(Modifier.height(8.dp))
                    com.workernotfound.app.core.designsystem.component.AppSmallButton(
                        stringResource(R.string.worker_application_cancel), onRequestCancel, color = AppColors.Danger)
                }
            }
        }
    }
}

@Composable
private fun SectionLoading() {
    Box(Modifier.fillMaxWidth().padding(AppDimens.screenPadding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.role.accent)
    }
}

@Composable
private fun SectionEmpty(@StringRes text: Int) {
    Text(stringResource(text), color = AppColors.TextSub, modifier = Modifier.padding(vertical = AppDimens.cardGap))
}

@Composable
private fun SectionError(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
        Text(message, color = AppColors.Danger)
        AppPrimaryButton(text = stringResource(R.string.worker_retry), onClick = onRetry)
    }
}

/** 제목 바로 아래에 근무 목록이 오도록, 지원 현황·출근 체크 진입점은 상단 메뉴에 둔다. */
@Composable
private fun WorkHeader(onApplicationsClick: () -> Unit, onAttendanceClick: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppDimens.screenPadding, end = 4.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "근무 관리",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.worker_work_menu), tint = AppColors.TextSub)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.worker_applications)) },
                    onClick = { menuOpen = false; onApplicationsClick() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.worker_work_attendance)) },
                    onClick = { menuOpen = false; onAttendanceClick() },
                )
            }
        }
    }
}

/** 가게명·상태 → 날짜·근무시간·시급 → 근무 취소·합의 순. */
@Composable
private fun WorkCard(work: WorkSchedule, onCancellation: (String) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = work.storeName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMain,
                modifier = Modifier.weight(1f),
            )
            WorkStatusBadge(work)
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${KoreanDateTime.workDate(work.workDate)} · ${work.timeRange}",
                color = AppColors.TextSub,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "₩${"%,d".format(work.hourlyWage)} / 시",
                color = AppTheme.role.accentPressed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(text = work.category, color = AppColors.TextPlaceholder, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        AppPrimaryButton(
            text = stringResource(R.string.cancellation_title),
            onClick = { onCancellation(work.id) },
        )
    }
}

/** 섹션과 일치하는 배지: 예정 / 출근 완료 / 근무 중. */
@Composable
private fun WorkStatusBadge(work: WorkSchedule) {
    when {
        work.status == WorkStatus.SCHEDULED -> AppBadge(
            text = stringResource(R.string.worker_work_badge_scheduled),
            background = AppColors.WorkerLight,
            contentColor = AppColors.WorkerEmphasis,
        )
        work.attendance?.status == "CHECKED_IN" -> AppBadge(
            text = stringResource(R.string.worker_work_badge_checked_in),
            background = AppColors.SuccessBg,
            contentColor = AppColors.Success,
        )
        else -> StatusBadge(status = AppStatus.WORKING)
    }
}
