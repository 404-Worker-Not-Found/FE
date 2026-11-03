package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.feature.worker.ui.component.*
import com.workernotfound.app.feature.worker.viewmodel.*

@Composable
fun WorkerHomeScreen(
    onJobClick: (String) -> Unit, onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier, viewModel: WorkerHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WorkerHomeContent(state, viewModel::toggleAvailableOnly, onJobClick,
        onNotificationsClick, viewModel::applyTo, viewModel::loadJobs, modifier)
}

@Composable
private fun WorkerHomeContent(
    uiState: WorkerHomeUiState, onToggleAvailable: (Boolean) -> Unit,
    onJobClick: (String) -> Unit, onNotificationsClick: () -> Unit,
    onApply: (String) -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier,
) {
    // Header scrolls with the feed so short windows and large text keep access to jobs.
    LazyVerticalGrid(columns = GridCells.Adaptive(AppDimens.jobCardMinWidth),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceXl),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spaceLg)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            HomeBrandHeader(stringResource(R.string.design_focus_worker_title),
                stringResource(R.string.design_focus_worker_subtitle), onNotificationsClick = onNotificationsClick)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            AvailabilityToggle(uiState.isAvailableOnly, onToggleAvailable)
        }
        when {
            uiState.isLoading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(AppDimens.spaceXxl), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.spaceLg)) {
                    Text(uiState.errorMessage, color = AppColors.TextSub)
                    AppPrimaryButton(stringResource(R.string.design_retry), onRetry)
                }
            }
            else -> {
                val jobs = uiState.urgentJobs + if (uiState.isAvailableOnly) emptyList()
                    else uiState.recommendedJobs
                val featured = jobs.firstOrNull()
                if (featured != null) {
                    item(key = "focus-header", span = { GridItemSpan(maxLineSpan) }) {
                        WorkerFocusJobCard(featured, { onJobClick(featured.id) }, { onApply(featured.id) })
                    }
                }
                if (jobs.size > 1) {
                    item(key = "other-jobs-heading", span = { GridItemSpan(maxLineSpan) }) {
                        SectionLabel(stringResource(R.string.design_focus_other_jobs))
                    }
                    items(jobs.drop(1), key = { it.id }) { job ->
                        WorkerFocusJobRow(job, { onJobClick(job.id) }, { onApply(job.id) })
                    }
                }
                if (uiState.urgentJobs.isEmpty() && (uiState.isAvailableOnly || uiState.recommendedJobs.isEmpty())) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(stringResource(R.string.backend_no_jobs), color = AppColors.TextSub)
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilityToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(color = AppColors.CardSurface) {
        Row(Modifier.fillMaxWidth().toggleable(checked, role = Role.Switch,
            onValueChange = onCheckedChange).padding(vertical = AppDimens.spaceXs),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(if (BuildConfig.USE_DEMO_DATA) R.string.design_urgent_filter
                    else R.string.backend_urgent_only), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(if (BuildConfig.USE_DEMO_DATA) R.string.design_urgent_filter_description
                    else R.string.backend_urgent_only_description), style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSub)
            }
            Spacer(Modifier.width(AppDimens.spaceSm))
            Switch(checked, onCheckedChange = null)
        }
    }
}

// Design-time fixtures only; previews never call a repository or submit an application.
@androidx.compose.ui.tooling.preview.Preview(name = "Compact 320", widthDp = 320, heightDp = 640)
@androidx.compose.ui.tooling.preview.Preview(name = "Phone 360", widthDp = 360, heightDp = 800)
@androidx.compose.ui.tooling.preview.Preview(name = "Wide 840", widthDp = 840, heightDp = 720)
@androidx.compose.ui.tooling.preview.Preview(name = "Large text", widthDp = 320, heightDp = 720, fontScale = 2f)
@Composable
private fun WorkerHomeDesignPreview() {
    val job = com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting(
        id = "preview", storeName = "동네 베이커리", category = "카페·베이커리",
        workTime = "오늘 14:00–18:00", hourlyWage = 14000,
        distanceText = "450m", isUrgent = true, deadlineText = "30분 후 마감")
    WorkerNotFoundTheme(AppRole.WORKER) {
        Surface(color = AppTheme.role.screenBackground) {
            WorkerHomeContent(WorkerHomeUiState(isLoading = false, urgentJobs = listOf(job),
                recommendedJobs = listOf(
                    job.copy(id = "recommended", isUrgent = false),
                    job.copy(id = "long-title", storeName = "긴 이름의 동네 베이커리 매장", isUrgent = false))),
                {}, {}, {}, {}, {})
        }
    }
}
