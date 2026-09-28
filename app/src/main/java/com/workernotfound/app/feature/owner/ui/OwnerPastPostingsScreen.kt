package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppStatus
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.StatusBadge
import com.workernotfound.app.feature.owner.domain.model.PastPosting
import com.workernotfound.app.feature.owner.domain.model.PastPostingStatus
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.viewmodel.OwnerPastPostingsUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerPastPostingsViewModel

/**
 * 지난 공고 목록 (UI spec 2-6). Shown as the 공고관리 tab (no [onBack]) and as the
 * standalone `OWNER_PAST_POSTINGS` destination (with [onBack]).
 */
@Composable
fun OwnerPastPostingsScreen(
    onPostingClick: (postingId: String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: OwnerPastPostingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    OwnerPastPostingsContent(
        uiState = uiState,
        onPostingClick = onPostingClick,
        onRetry = viewModel::refresh,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun OwnerPastPostingsContent(
    uiState: OwnerPastPostingsUiState,
    onPostingClick: (String) -> Unit,
    onRetry: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(AppColors.Surface)) {
        if (onBack != null) {
            AppTopBar(title = "지난 공고", onBack = onBack, modifier = Modifier.statusBarsPadding())
        } else {
            Text(
                text = "지난 공고 관리",
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
        }
        when {
            uiState.isLoading -> OwnerLoadingBox()
            uiState.errorMessage != null -> OwnerMessageBox(
                message = uiState.errorMessage,
                actionText = "다시 시도",
                onAction = onRetry,
            )
            uiState.postings.isEmpty() -> OwnerMessageBox(message = "지난 공고가 없어요.")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppDimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
            ) {
                items(items = uiState.postings, key = { it.postingId }) { posting ->
                    PastPostingCard(posting = posting, onClick = { onPostingClick(posting.postingId) })
                }
            }
        }
    }
}

/** 공고 카드: 날짜, 업무, 매칭된 알바생 이름, 상태(완료/노쇼/분쟁). */
@Composable
private fun PastPostingCard(posting: PastPosting, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = OwnerFormat.date(posting.workStartMillis),
                color = AppColors.TextSub,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )
            StatusBadge(status = posting.status.toBadge())
        }
        Spacer(Modifier.height(8.dp))
        Text(text = posting.workSummary, style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
        Spacer(Modifier.height(4.dp))
        Text(text = "매칭 알바생 · ${posting.workerName}", color = AppColors.TextSub, fontSize = 13.sp)
    }
}

internal fun PastPostingStatus.toBadge(): AppStatus = when (this) {
    PastPostingStatus.COMPLETED -> AppStatus.DONE
    PastPostingStatus.NO_SHOW -> AppStatus.NO_SHOW
    PastPostingStatus.DISPUTE -> AppStatus.DISPUTE
}
