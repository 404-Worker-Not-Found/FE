package com.workernotfound.app.feature.worker.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MyLocation
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.ui.component.JobFilterSheet
import com.workernotfound.app.feature.worker.ui.component.LiveJobMap
import com.workernotfound.app.feature.worker.ui.component.WorkerRecommendedJobCard
import com.workernotfound.app.feature.worker.ui.component.WorkerUrgentJobCard
import com.workernotfound.app.feature.worker.viewmodel.JobViewMode
import com.workernotfound.app.feature.worker.viewmodel.SearchMode
import com.workernotfound.app.feature.worker.viewmodel.WorkerJobSearchUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerJobSearchViewModel

@Composable
fun WorkerJobSearchScreen(
    onJobClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerJobSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) viewModel.locate() else viewModel.locationDenied()
    }
    val requestLocation = {
        permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    // Reload whenever the tab is shown again or a detail screen returns here (new OPEN postings appear).
    androidx.lifecycle.compose.LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    if (uiState.isFilterSheetOpen) {
        JobFilterSheet(
            current = uiState.filter,
            usesCurrentLocation = uiState.usesCurrentLocation,
            onUseCurrentLocation = { viewModel.dismissFilterSheet(); requestLocation() },
            onUseDefaultLocation = viewModel::useDefaultLocation,
            onApply = viewModel::applyFilter,
            onDismiss = viewModel::dismissFilterSheet,
        )
    }

    WorkerJobSearchContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSubmitQuery = viewModel::submitQuery,
        onClearQuery = viewModel::clearQuery,
        onOpenFilter = viewModel::openFilterSheet,
        onSetViewMode = viewModel::setViewMode,
        onJobClick = onJobClick,
        onApply = viewModel::applyTo,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onLocate = requestLocation,
        modifier = modifier,
    )
}

@Composable
private fun WorkerJobSearchContent(
    uiState: WorkerJobSearchUiState,
    onQueryChange: (String) -> Unit,
    onSubmitQuery: () -> Unit,
    onClearQuery: () -> Unit,
    onOpenFilter: () -> Unit,
    onSetViewMode: (JobViewMode) -> Unit,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLocate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(AppDimens.screenPadding)) {
            SearchBarRow(
                query = uiState.query,
                filterActive = uiState.filter.isActive || uiState.usesCurrentLocation,
                onQueryChange = onQueryChange,
                onSubmit = onSubmitQuery,
                onClear = onClearQuery,
                onOpenFilter = onOpenFilter,
            )
            SearchStatus(uiState)
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
            // Pull-to-refresh only on the list; on the map a downward drag must pan the map.
            uiState.viewMode == JobViewMode.LIST -> {
                @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) { JobList(uiState, onJobClick, onApply) }
            }
            else -> MapView(uiState, onJobClick, onApply, onLocate)
        }
    }
}

@Composable
private fun SearchStatus(uiState: WorkerJobSearchUiState) {
    val text = when (uiState.searchMode) {
        SearchMode.NONE -> null
        SearchMode.STORE -> stringResource(R.string.worker_search_store_result, uiState.submittedQuery)
        SearchMode.AREA -> stringResource(R.string.worker_search_area_result, uiState.submittedQuery)
        SearchMode.NO_RESULT -> stringResource(R.string.worker_search_no_result, uiState.submittedQuery)
    }
    val error = if (uiState.locationError) stringResource(R.string.map_address_failed) else null
    listOfNotNull(text, error).forEach {
        Spacer(Modifier.height(8.dp))
        Text(text = it, color = if (it == error) AppColors.Danger else AppColors.TextSub, fontSize = 12.sp)
    }
}

@Composable
private fun SearchBarRow(
    query: String,
    filterActive: Boolean,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    onOpenFilter: () -> Unit,
) {
    val focus = LocalFocusManager.current
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
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(text = stringResource(R.string.worker_search_hint), color = AppColors.TextPlaceholder, fontSize = 13.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = AppColors.TextMain, fontSize = 13.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus(); onSubmit() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.worker_search_clear),
                    tint = AppColors.TextSub,
                    modifier = Modifier.size(18.dp).clickable { focus.clearFocus(); onClear() },
                )
            }
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
                contentDescription = stringResource(R.string.worker_search_filter),
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
            text = stringResource(R.string.worker_search_total, totalCount),
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
        jobSection(R.string.worker_home_recommended, uiState.recommendedJobs, onJobClick, onApply)
        jobSection(R.string.worker_search_others, uiState.otherJobs, onJobClick, onApply)
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

private fun androidx.compose.foundation.lazy.LazyListScope.jobSection(
    title: Int,
    jobs: List<WorkerJobPosting>,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
) {
    if (jobs.isEmpty()) return
    item(key = "section-$title") { SectionLabel(text = stringResource(title)) }
    items(items = jobs, key = { it.id }) { job ->
        WorkerRecommendedJobCard(job = job, onClick = { onJobClick(job.id) }, onApply = { onApply(job.id) })
    }
}

@Composable
private fun MapView(
    uiState: WorkerJobSearchUiState,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
    onLocate: () -> Unit,
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize()) {
        LiveJobMap(
            jobs = uiState.visibleJobs,
            selectedId = selectedId,
            onSelect = { selectedId = it },
            latitude = uiState.originLatitude,
            longitude = uiState.originLongitude,
            onJobClick = onJobClick,
            onApply = onApply,
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(AppColors.CardSurface, RoundedCornerShape(AppRadius.pill))
                .border(AppDimens.borderWidth, AppColors.Border, RoundedCornerShape(AppRadius.pill))
                .clickable(enabled = !uiState.locating, onClick = onLocate)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = AppTheme.role.accentPressed, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(if (uiState.locating) R.string.map_locating else R.string.worker_map_my_location),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMain,
            )
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
