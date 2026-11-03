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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppBadge
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppStatus
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.core.designsystem.component.StatusBadge
import com.workernotfound.app.feature.worker.domain.model.WorkSchedule
import com.workernotfound.app.feature.worker.viewmodel.WorkerWorkUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerWorkViewModel

@Composable
fun WorkerWorkScreen(
    onTrustClick: () -> Unit,
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
        uiState = uiState, onRetry = viewModel::load, onTrustClick = onTrustClick,
        onApplicationsClick = onApplicationsClick, modifier = modifier,
        onCancellation = onCancellation,
    )
}

@Composable
private fun WorkerWorkContent(
    uiState: WorkerWorkUiState,
    onRetry: () -> Unit,
    onTrustClick: () -> Unit,
    onApplicationsClick: () -> Unit,
    onCancellation: (String) -> Unit,
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
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = AppDimens.screenPadding)) {
            TextButton(onClick = onApplicationsClick, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.worker_applications))
            }
            TextButton(onClick = onTrustClick, modifier = Modifier.weight(1f)) {
                Text(stringResource(if (com.workernotfound.app.BuildConfig.USE_DEMO_DATA) R.string.worker_trust_history else R.string.attendance_title))
            }
        }
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
                contentPadding = PaddingValues(AppDimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
            ) {
                if (uiState.inProgress.isNotEmpty()) {
                    item { SectionLabel(text = "진행 중 근무") }
                    items(items = uiState.inProgress, key = { it.id }) { WorkCard(it, onCancellation) }
                }
                if (uiState.scheduled.isNotEmpty()) {
                    item { SectionLabel(text = "예정 근무") }
                    items(items = uiState.scheduled, key = { it.id }) { WorkCard(it, onCancellation) }
                }
            }
        }
    }
}

@Composable
private fun WorkCard(work: WorkSchedule, onCancellation: (String) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        if (!com.workernotfound.app.BuildConfig.USE_DEMO_DATA) TextButton({ onCancellation(work.id) }) { Text(stringResource(R.string.cancellation_title)) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = work.storeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(2.dp))
                Text(text = "${work.category} · ${work.workDate}", color = AppColors.TextSub, fontSize = 12.sp)
            }
            if (work.attendance?.checkedIn == true && work.attendance.status != "IN_PROGRESS")
                Text(stringResource(R.string.attendance_complete), color = AppColors.Success)
            else WorkStatusBadge(work)
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = work.timeRange, color = AppColors.TextSub, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            Text(
                text = "₩${"%,d".format(work.hourlyWage)} / 시",
                color = AppTheme.role.accentPressed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun WorkStatusBadge(work: WorkSchedule) {
    if (work.attendance?.status == "SCHEDULED") {
        AppBadge(text = stringResource(R.string.attendance_before_work), background = AppColors.WorkerLight, contentColor = AppColors.WorkerEmphasis)
        return
    }
    when (work.status) {
        com.workernotfound.app.feature.worker.domain.model.WorkStatus.IN_PROGRESS ->
            StatusBadge(status = AppStatus.WORKING)
        com.workernotfound.app.feature.worker.domain.model.WorkStatus.SCHEDULED ->
            AppBadge(text = "예정", background = AppColors.WorkerLight, contentColor = AppColors.WorkerEmphasis)
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
