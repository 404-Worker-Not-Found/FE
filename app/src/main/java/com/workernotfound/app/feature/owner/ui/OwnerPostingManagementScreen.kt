package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import com.workernotfound.app.feature.owner.ui.component.OwnerJobCard
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.viewmodel.OwnerHomeViewModel

internal val PostingStatus.needsPayment: Boolean
    get() = this == PostingStatus.PAYMENT_PENDING || this == PostingStatus.FUNDING_BLOCKED

@Composable
fun OwnerPostingManagementScreen(
    onCreatePosting: () -> Unit,
    onPostingClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit,
    onPastPostingClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(AppDimens.screenPadding), verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            Text(stringResource(R.string.owner_postings_title), style = MaterialTheme.typography.titleLarge)
            AppPrimaryButton(stringResource(R.string.publication_title), onCreatePosting)
        }
        TabRow(selectedTabIndex = selectedTab) {
            listOf(R.string.owner_postings_current, R.string.owner_postings_past).forEachIndexed { index, label ->
                Tab(selected = selectedTab == index, unselectedContentColor = AppColors.TextSub, onClick = { selectedTab = index }, text = { Text(stringResource(label)) })
            }
        }
        if (selectedTab == 1) {
            OwnerPastPostingsScreen(onPostingClick = onPastPostingClick, showHeader = false)
        } else when {
            state.isLoading -> OwnerLoadingBox()
            state.errorMessage != null -> OwnerMessageBox(
                message = requireNotNull(state.errorMessage), actionText = stringResource(R.string.owner_postings_retry),
                onAction = viewModel::loadPostings,
            )
            else -> {
                val postings = state.postings.filter { it.status != PostingStatus.CLOSED && it.status != PostingStatus.DONE }
                if (postings.isEmpty()) OwnerMessageBox(message = stringResource(R.string.owner_postings_empty))
                else LazyColumn(contentPadding = PaddingValues(AppDimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
                    items(postings, key = { it.id }) { posting ->
                        OwnerJobCard(posting, onClick = {
                            if (posting.status.needsPayment) onPaymentClick(posting.id) else onPostingClick(posting.id)
                        })
                    }
                }
            }
        }
    }
}
