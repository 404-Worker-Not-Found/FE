package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.AppStatus
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.StatusBadge
import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerApplication
import com.workernotfound.app.feature.worker.viewmodel.WorkerApplicationUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerApplicationViewModel

@Composable
fun WorkerApplicationScreen(
    onBack: () -> Unit,
    onMatchClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerApplicationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkerApplicationContent(
        uiState = uiState,
        onBack = onBack,
        onMatchClick = onMatchClick,
        onRequestCancel = viewModel::requestCancel,
        onDismissCancel = viewModel::dismissCancel,
        onConfirmCancel = viewModel::confirmCancel,
        onRetry = viewModel::load,
        modifier = modifier,
    )
}

@Composable
private fun WorkerApplicationContent(
    uiState: WorkerApplicationUiState,
    onBack: () -> Unit,
    onMatchClick: (String) -> Unit,
    onRequestCancel: (String) -> Unit,
    onDismissCancel: () -> Unit,
    onConfirmCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.WorkerSurface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "지원 현황", onBack = onBack)
        when {
            uiState.isLoading -> LoadingBox()
            uiState.errorMessage != null -> ErrorBox(uiState.errorMessage, onRetry)
            uiState.applications.isEmpty() -> EmptyBox()
            else -> ApplicationList(
                applications = uiState.applications,
                onMatchClick = onMatchClick,
                onRequestCancel = onRequestCancel,
            )
        }
    }

    if (uiState.pendingCancelId != null) {
        CancelConfirmDialog(onConfirm = onConfirmCancel, onDismiss = onDismissCancel)
    }
}

@Composable
private fun ApplicationList(
    applications: List<WorkerApplication>,
    onMatchClick: (String) -> Unit,
    onRequestCancel: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
    ) {
        items(items = applications, key = { it.id }) { app ->
            ApplicationCard(
                app = app,
                onMatchClick = { onMatchClick(app.id) },
                onRequestCancel = { onRequestCancel(app.id) },
            )
        }
    }
}

@Composable
private fun ApplicationCard(
    app: WorkerApplication,
    onMatchClick: () -> Unit,
    onRequestCancel: () -> Unit,
) {
    // 매칭완료 또는 재매칭 제안 건은 탭 시 매칭 결과(3-5)로 이동
    val navigable = app.status == ApplicationStatus.MATCHED || app.hasRematchOffer
    val cardModifier = Modifier
        .fillMaxWidth()
        .then(if (navigable) Modifier.clickable(onClick = onMatchClick) else Modifier)

    AppCard(modifier = cardModifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.storeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${app.category} · ${app.workDate}",
                    color = AppColors.TextSub,
                    fontSize = 12.sp,
                )
            }
            StatusBadge(status = app.status.toBadge())
        }
        Spacer(Modifier.height(10.dp))
        Text(text = "${app.workTimeRange} · 지원 ${app.appliedAtText}", color = AppColors.TextSub, fontSize = 12.sp)

        if (app.hasRematchOffer) {
            Spacer(Modifier.height(10.dp))
            RematchOfferRow()
        }
        if (app.canCancel) {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onRequestCancel) {
                Text(text = "지원 취소", color = AppColors.TextSub, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RematchOfferRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.DangerBg, RoundedCornerShape(AppRadius.inner))
            .padding(vertical = 10.dp, horizontal = 12.dp),
    ) {
        Text(
            text = "🔔 재매칭 기회가 도착했어요 · 눌러서 확인",
            color = AppColors.Danger,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun ApplicationStatus.toBadge(): AppStatus = when (this) {
    ApplicationStatus.APPLYING -> AppStatus.APPLYING
    ApplicationStatus.MATCHING -> AppStatus.MATCHING
    ApplicationStatus.MATCHED -> AppStatus.MATCHED
    ApplicationStatus.NOT_SELECTED -> AppStatus.NOT_SELECTED
}

@Composable
private fun CancelConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.CardSurface,
        title = { Text(text = "지원 취소", fontWeight = FontWeight.Bold, color = AppColors.TextMain) },
        text = { Text(text = "이 공고 지원을 취소할까요?", color = AppColors.TextSub) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "지원 취소", color = AppColors.Danger, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "닫기", color = AppColors.TextSub)
            }
        },
    )
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
        Text(text = "아직 지원한 공고가 없어요.", color = AppColors.TextSub)
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(AppDimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, color = AppColors.TextSub)
        Spacer(Modifier.height(12.dp))
        AppPrimaryButton(text = "다시 시도", onClick = onRetry)
    }
}
