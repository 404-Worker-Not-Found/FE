package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.workernotfound.app.feature.worker.viewmodel.WorkerWorkUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerWorkViewModel

@Composable
fun WorkerWorkScreen(
    onAttendanceClick: () -> Unit,
    onApplicationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerWorkViewModel = hiltViewModel(),
    onCancellation: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.load() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    WorkerWorkContent(
        uiState = uiState, onRetry = viewModel::load, onAttendanceClick = onAttendanceClick,
        onApplicationsClick = onApplicationsClick, modifier = modifier,
        onCancellation = onCancellation,
    )
}

@Composable
private fun WorkerWorkContent(
    uiState: WorkerWorkUiState,
    onRetry: () -> Unit,
    onAttendanceClick: () -> Unit,
    onApplicationsClick: () -> Unit,
    onCancellation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WorkHeader(onApplicationsClick = onApplicationsClick, onAttendanceClick = onAttendanceClick)
        when {
            uiState.errorMessage != null && !uiState.isLoading -> Column(
                modifier = Modifier.fillMaxSize().padding(AppDimens.screenPadding),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(uiState.errorMessage ?: stringResource(R.string.worker_schedule_load_error), color = AppColors.TextSub)
                AppPrimaryButton(text = stringResource(R.string.worker_retry), onClick = onRetry)
            }
            uiState.isLoading -> LoadingBox()
            uiState.scheduled.isEmpty() && uiState.inProgress.isEmpty() -> EmptyBox()
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = AppDimens.screenPadding,
                    end = AppDimens.screenPadding,
                    bottom = AppDimens.screenPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
            ) {
                if (uiState.inProgress.isNotEmpty()) {
                    item { SectionLabel(text = stringResource(R.string.worker_work_in_progress)) }
                    items(items = uiState.inProgress, key = { it.id }) { WorkCard(it, onCancellation) }
                }
                if (uiState.scheduled.isNotEmpty()) {
                    item { SectionLabel(text = stringResource(R.string.worker_work_scheduled)) }
                    items(items = uiState.scheduled, key = { it.id }) { WorkCard(it, onCancellation) }
                }
            }
        }
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

/** 가게명·상태 → 날짜·근무시간·시급 순. 근무 취소·합의는 카드 메뉴에 둔다. */
@Composable
private fun WorkCard(work: WorkSchedule, onCancellation: (String) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
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
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.worker_work_card_menu), tint = AppColors.TextSub)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.cancellation_title)) },
                        onClick = { menuOpen = false; onCancellation(work.id) },
                    )
                }
            }
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

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.role.accent)
    }
}

@Composable
private fun EmptyBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "예정된 근무가 없어요.", color = AppColors.TextSub)
    }
}
