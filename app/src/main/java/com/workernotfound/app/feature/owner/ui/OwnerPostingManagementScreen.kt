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
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val currentPostings = state.postings.filter { it.status != PostingStatus.CLOSED && it.status != PostingStatus.DONE }
    val countLoaded = !state.isLoading && state.errorMessage == null
    Column(modifier.fillMaxSize()) {
        // 제목 + 작은 등록 버튼, 그 아래 알바생 탐색의 리스트/지도와 같은 알약형 토글.
        Row(
            Modifier.fillMaxWidth().padding(start = AppDimens.screenPadding, end = AppDimens.screenPadding, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.owner_postings_title), fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = AppColors.TextMain, modifier = Modifier.weight(1f))
            Button(
                onClick = onCreatePosting,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(AppRadius.pill),
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.role.accent, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) { Text(stringResource(R.string.owner_postings_create_action), fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }
        Row(
            Modifier.padding(start = AppDimens.screenPadding, end = AppDimens.screenPadding, top = 14.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PostingTabPill(
                label = if (countLoaded) stringResource(R.string.owner_postings_current_count, currentPostings.size)
                else stringResource(R.string.owner_postings_current),
                selected = selectedTab == 0,
            ) { selectedTab = 0 }
            PostingTabPill(stringResource(R.string.owner_postings_past), selected = selectedTab == 1) { selectedTab = 1 }
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
                val postings = currentPostings
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

/** 알바생 탐색의 리스트/지도 토글과 같은 알약 모양 탭. */
@Composable
private fun PostingTabPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = if (selected) Color.White else AppColors.TextSub,
        modifier = Modifier
            .background(if (selected) AppTheme.role.accent else AppColors.CardSurface, RoundedCornerShape(AppRadius.pill))
            .border(AppDimens.borderWidth, if (selected) AppTheme.role.accent else AppColors.Border, RoundedCornerShape(AppRadius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
