package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.feature.owner.domain.model.OwnerHomeSummary
import com.workernotfound.app.feature.owner.ui.component.OwnerJobCard
import com.workernotfound.app.feature.owner.viewmodel.*

@Composable
fun OwnerHomeScreen(onCreatePosting: () -> Unit, onPostingClick: (String) -> Unit,
    modifier: Modifier = Modifier, viewModel: OwnerHomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) { viewModel.refresh(); onPauseOrDispose { } }
    OwnerHomeContent(state, onCreatePosting, onPostingClick, viewModel::loadPostings, modifier)
}

@Composable
private fun OwnerHomeContent(uiState: OwnerHomeUiState, onCreatePosting: () -> Unit,
    onPostingClick: (String) -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(columns = GridCells.Adaptive(AppDimens.jobCardMinWidth),
        modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(AppDimens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceLg),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spaceLg)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            HomeBrandHeader(stringResource(R.string.design_focus_owner_title),
                stringResource(R.string.design_focus_owner_subtitle))
        }
        item(span = { GridItemSpan(maxLineSpan) }) { SummaryStrip(uiState.summary, onCreatePosting) }
        item(span = { GridItemSpan(maxLineSpan) }) { SectionLabel(stringResource(R.string.design_my_postings)) }
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
            uiState.postings.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(R.string.design_no_postings), color = AppColors.TextSub)
            }
            else -> items(uiState.postings, key = { it.id }) { posting ->
                OwnerJobCard(posting, onClick = { onPostingClick(posting.id) })
            }
        }
    }
}

@Composable
private fun SummaryStrip(summary: OwnerHomeSummary, onCreatePosting: () -> Unit) {
    Surface(color = AppTheme.role.accent, shape = MaterialTheme.shapes.medium,
        contentColor = AppTheme.role.onAccent) {
        Column(Modifier.fillMaxWidth().padding(AppDimens.spaceXl)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceLg)) {
                SummaryItem(summary.activePostingCount, stringResource(R.string.design_active_postings), Modifier.weight(1f))
                SummaryItem(summary.totalApplicantCount, stringResource(R.string.design_total_applicants), Modifier.weight(1f))
            }
            Spacer(Modifier.height(AppDimens.spaceLg))
            HorizontalDivider(color = AppColors.OnOwnerAccentSub.copy(alpha = 0.25f))
            Spacer(Modifier.height(AppDimens.spaceLg))
            Button(onClick = onCreatePosting, modifier = Modifier.fillMaxWidth().heightIn(min = AppDimens.touchTarget),
                shape = MaterialTheme.shapes.small, colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.FocusSurface, contentColor = AppColors.FocusContent)) {
                Text(stringResource(R.string.design_create_posting), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.Add, null, Modifier.size(AppDimens.iconSize), tint = AppTheme.role.accent)
            }
        }
    }
}

@Composable
private fun SummaryItem(value: Int?, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AppColors.OnOwnerAccentSub)
        Text(value?.toString() ?: stringResource(R.string.backend_not_provided),
            style = if (value == null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.headlineLarge,
            color = AppTheme.role.onAccent)
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Compact 320", widthDp = 320, heightDp = 640)
@androidx.compose.ui.tooling.preview.Preview(name = "Phone 360", widthDp = 360, heightDp = 800)
@androidx.compose.ui.tooling.preview.Preview(name = "Wide 840", widthDp = 840, heightDp = 720)
@androidx.compose.ui.tooling.preview.Preview(name = "Large text", widthDp = 320, heightDp = 720, fontScale = 2f)
@Composable
private fun OwnerHomeDesignPreview() {
    val posting = com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting(
        "preview", "홀 서빙 및 매장 정리", "11월 2일", "14:00–18:00", 14000,
        com.workernotfound.app.feature.owner.domain.model.PostingStatus.RECRUITING, 3, true)
    WorkerNotFoundTheme(AppRole.OWNER) {
        Surface(color = AppTheme.role.screenBackground) {
            OwnerHomeContent(OwnerHomeUiState(isLoading = false, summary = OwnerHomeSummary(2, 5),
                postings = listOf(posting, posting.copy(id = "second", isUrgent = false))), {}, {}, {})
        }
    }
}
