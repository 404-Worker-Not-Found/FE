package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.ui.component.JobFilterSheet
import com.workernotfound.app.feature.worker.ui.component.WorkerJobMapView
import com.workernotfound.app.feature.worker.ui.component.WorkerRecommendedJobCard
import com.workernotfound.app.feature.worker.ui.component.WorkerUrgentJobCard
import com.workernotfound.app.feature.worker.viewmodel.JobViewMode
import com.workernotfound.app.feature.worker.viewmodel.WorkerJobSearchUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerJobSearchViewModel

@Composable
fun WorkerJobSearchScreen(
    onJobClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerJobSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isFilterSheetOpen) {
        JobFilterSheet(
            current = uiState.filter,
            onApply = viewModel::applyFilter,
            onDismiss = viewModel::dismissFilterSheet,
        )
    }

    WorkerJobSearchContent(
        uiState = uiState,
        onOpenFilter = viewModel::openFilterSheet,
        onSetViewMode = viewModel::setViewMode,
        onJobClick = onJobClick,
        onApply = viewModel::applyTo,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
private fun WorkerJobSearchContent(
    uiState: WorkerJobSearchUiState,
    onOpenFilter: () -> Unit,
    onSetViewMode: (JobViewMode) -> Unit,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(AppDimens.screenPadding)) {
            SearchBarRow(filterActive = uiState.filter.isActive, onOpenFilter = onOpenFilter)
            Spacer(Modifier.height(12.dp))
            ModeRow(
                totalCount = uiState.totalCount,
                viewMode = uiState.viewMode,
                onSetViewMode = onSetViewMode,
            )
        }

        when {
            uiState.isLoading -> LoadingBox()
            uiState.errorMessage != null -> ErrorBox(uiState.errorMessage, onRetry)
            uiState.viewMode == JobViewMode.LIST -> JobList(uiState, onJobClick, onApply)
            else -> MapView(uiState, onJobClick, onApply)
        }
    }
}

@Composable
private fun SearchBarRow(filterActive: Boolean, onOpenFilter: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .background(AppColors.CardSurface, RoundedCornerShape(AppRadius.pill))
                .border(AppDimens.borderWidth, AppColors.Border, RoundedCornerShape(AppRadius.pill))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = AppColors.TextPlaceholder, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = "동네 · 가게 검색", color = AppColors.TextPlaceholder, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (filterActive) AppTheme.role.accent else AppColors.CardSurface,
                    RoundedCornerShape(AppRadius.inner),
                )
                .border(AppDimens.borderWidth, AppColors.Border, RoundedCornerShape(AppRadius.inner))
                .clickable(onClick = onOpenFilter),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Tune,
                contentDescription = "필터",
                tint = if (filterActive) Color.White else AppColors.TextSub,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ModeRow(totalCount: Int, viewMode: JobViewMode, onSetViewMode: (JobViewMode) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "총 ${totalCount}개 공고",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextSub,
        )
        Spacer(Modifier.weight(1f))
        ModeToggle("리스트", viewMode == JobViewMode.LIST) { onSetViewMode(JobViewMode.LIST) }
        Spacer(Modifier.width(6.dp))
        ModeToggle("지도", viewMode == JobViewMode.MAP) { onSetViewMode(JobViewMode.MAP) }
    }
}

@Composable
private fun ModeToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (selected) Color.White else AppColors.TextSub,
        modifier = Modifier
            .background(
                if (selected) AppTheme.role.accent else AppColors.CardSurface,
                RoundedCornerShape(AppRadius.pill),
            )
            .border(AppDimens.borderWidth, AppColors.Border, RoundedCornerShape(AppRadius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun JobList(
    uiState: WorkerJobSearchUiState,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AppDimens.screenPadding,
            end = AppDimens.screenPadding,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
    ) {
        if (uiState.urgentJobs.isNotEmpty()) {
            item { SectionLabel(text = "🚨 긴급 공고") }
            items(items = uiState.urgentJobs, key = { it.id }) { job ->
                WorkerUrgentJobCard(
                    job = job,
                    onClick = { onJobClick(job.id) },
                    onApply = { onApply(job.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (uiState.recommendedJobs.isNotEmpty()) {
            item { SectionLabel(text = "추천 공고") }
            items(items = uiState.recommendedJobs, key = { it.id }) { job ->
                WorkerRecommendedJobCard(
                    job = job,
                    onClick = { onJobClick(job.id) },
                    onApply = { onApply(job.id) },
                )
            }
        }
        if (uiState.totalCount == 0) {
            item {
                Text(
                    text = "조건에 맞는 공고가 없어요.",
                    color = AppColors.TextSub,
                    modifier = Modifier.padding(top = 40.dp),
                )
            }
        }
    }
}

@Composable
private fun MapView(
    uiState: WorkerJobSearchUiState,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
) {
    val jobs: List<WorkerJobPosting> = uiState.urgentJobs + uiState.recommendedJobs
    var selectedId by remember { mutableStateOf<String?>(null) }
    WorkerJobMapView(
        jobs = jobs,
        selectedJobId = selectedId,
        onSelectPin = { selectedId = it },
        onJobClick = onJobClick,
        onApply = onApply,
    )
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
