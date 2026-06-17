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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.worker.ui.component.WorkerRecommendedJobCard
import com.workernotfound.app.feature.worker.ui.component.WorkerUrgentJobCard
import com.workernotfound.app.feature.worker.viewmodel.WorkerHomeUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerHomeViewModel

/** Stateful entry: wires the ViewModel to the stateless content (UI spec 3-1). */
@Composable
fun WorkerHomeScreen(
    onJobClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkerHomeContent(
        uiState = uiState,
        onToggleAvailable = viewModel::toggleAvailableOnly,
        onJobClick = onJobClick,
        onApply = viewModel::applyTo,
        onRetry = viewModel::loadJobs,
        modifier = modifier,
    )
}

@Composable
private fun WorkerHomeContent(
    uiState: WorkerHomeUiState,
    onToggleAvailable: (Boolean) -> Unit,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(
                start = AppDimens.screenPadding,
                end = AppDimens.screenPadding,
                top = 20.dp,
                bottom = 8.dp,
            ),
        ) {
            HomeHeader()
            Spacer(Modifier.height(12.dp))
            AvailabilityToggleCard(
                checked = uiState.isAvailableOnly,
                onCheckedChange = onToggleAvailable,
            )
        }

        when {
            uiState.isLoading -> LoadingBox()
            uiState.errorMessage != null -> ErrorBox(uiState.errorMessage, onRetry)
            else -> JobLists(
                uiState = uiState,
                onJobClick = onJobClick,
                onApply = onApply,
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "내 주변 알바", color = AppColors.TextSub, fontSize = 12.sp)
            Text(
                text = "이건운님 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMain,
            )
        }
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "알림",
            tint = AppColors.TextSub,
        )
    }
}

@Composable
private fun AvailabilityToggleCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), radius = AppRadius.card) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "⚡ 즉시 지원 가능",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "켜면 즉시 지원 가능한 공고만 보여드려요",
                    color = AppColors.TextSub,
                    fontSize = 12.sp,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppTheme.role.accent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = AppColors.Border,
                    uncheckedBorderColor = AppColors.Border,
                ),
            )
        }
    }
}

@Composable
private fun JobLists(
    uiState: WorkerHomeUiState,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
    ) {
        if (uiState.urgentJobs.isNotEmpty()) {
            item {
                SectionLabel(
                    text = "🚨 긴급 공고",
                    modifier = Modifier.padding(horizontal = AppDimens.screenPadding),
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AppDimens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
                ) {
                    items(items = uiState.urgentJobs, key = { it.id }) { job ->
                        WorkerUrgentJobCard(
                            job = job,
                            onClick = { onJobClick(job.id) },
                            onApply = { onApply(job.id) },
                        )
                    }
                }
            }
        }

        if (!uiState.isAvailableOnly) {
            item {
                SectionLabel(
                    text = "추천 공고",
                    modifier = Modifier.padding(horizontal = AppDimens.screenPadding),
                )
                Spacer(Modifier.height(8.dp))
            }
            items(items = uiState.recommendedJobs, key = { it.id }) { job ->
                WorkerRecommendedJobCard(
                    job = job,
                    onClick = { onJobClick(job.id) },
                    onApply = { onApply(job.id) },
                    modifier = Modifier.padding(horizontal = AppDimens.screenPadding),
                )
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.role.accent)
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
