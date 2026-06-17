package com.workernotfound.app.feature.owner.ui

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
import androidx.compose.material3.Text
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
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.owner.domain.model.OwnerHomeSummary
import com.workernotfound.app.feature.owner.ui.component.OwnerJobCard
import com.workernotfound.app.feature.owner.viewmodel.OwnerHomeUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerHomeViewModel

/** Stateful entry: wires the ViewModel to the stateless content (UI spec 2-1). */
@Composable
fun OwnerHomeScreen(
    onCreatePosting: () -> Unit,
    onPostingClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OwnerHomeContent(
        uiState = uiState,
        onCreatePosting = onCreatePosting,
        onPostingClick = onPostingClick,
        onRetry = viewModel::loadPostings,
        modifier = modifier,
    )
}

@Composable
private fun OwnerHomeContent(
    uiState: OwnerHomeUiState,
    onCreatePosting: () -> Unit,
    onPostingClick: (String) -> Unit,
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
            Text(
                text = "사장님, 안녕하세요 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMain,
            )
            Spacer(Modifier.height(12.dp))
            SummaryStrip(uiState.summary)
        }

        when {
            uiState.isLoading -> LoadingBox()
            uiState.errorMessage != null -> ErrorBox(uiState.errorMessage, onRetry)
            else -> PostingList(
                uiState = uiState,
                onCreatePosting = onCreatePosting,
                onPostingClick = onPostingClick,
            )
        }
    }
}

@Composable
private fun SummaryStrip(summary: OwnerHomeSummary) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SummaryItem(
                value = "${summary.activePostingCount}",
                label = "모집중 공고",
                modifier = Modifier.weight(1f),
            )
            SummaryItem(
                value = "${summary.totalApplicantCount}",
                label = "총 지원자",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = AppTheme.role.accent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = label, color = AppColors.TextSub, fontSize = 12.sp)
    }
}

@Composable
private fun PostingList(
    uiState: OwnerHomeUiState,
    onCreatePosting: () -> Unit,
    onPostingClick: (String) -> Unit,
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
        item {
            SectionLabel(text = "내 공고")
            Spacer(Modifier.height(8.dp))
        }
        items(items = uiState.postings, key = { it.id }) { posting ->
            OwnerJobCard(posting = posting, onClick = { onPostingClick(posting.id) })
        }
        item {
            Spacer(Modifier.height(8.dp))
            AppPrimaryButton(text = "⚡ 긴급 공고 등록", onClick = onCreatePosting)
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
