package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.core.designsystem.component.UrgentBadge
import com.workernotfound.app.feature.owner.domain.model.ApplicantBoard
import com.workernotfound.app.feature.owner.ui.component.ApplicantCard
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.viewmodel.OwnerApplicantsUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerApplicantsViewModel

/** 지원자 목록 (UI spec 2-3). Opened from an owner home posting card. */
@Composable
fun OwnerApplicantsScreen(
    onBack: () -> Unit,
    onApplicantClick: (postingId: String, applicantId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerApplicantsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    OwnerApplicantsContent(
        uiState = uiState,
        onBack = onBack,
        onApplicantClick = onApplicantClick,
        onRetry = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
private fun OwnerApplicantsContent(
    uiState: OwnerApplicantsUiState,
    onBack: () -> Unit,
    onApplicantClick: (String, String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "지원자 관리", onBack = onBack)
        val board = uiState.board
        when {
            uiState.isLoading -> OwnerLoadingBox()
            board == null -> OwnerMessageBox(
                message = uiState.errorMessage ?: "지원자를 불러오지 못했어요.",
                actionText = "다시 시도",
                onAction = onRetry,
            )
            else -> ApplicantList(board = board, onApplicantClick = onApplicantClick)
        }
    }
}

@Composable
private fun ApplicantList(
    board: ApplicantBoard,
    onApplicantClick: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
    ) {
        item { PostingHeader(board) }
        item { SectionLabel(text = "지원자 ${board.applicants.size}명 · 매칭 점수순") }
        if (board.applicants.isEmpty()) {
            item { OwnerMessageBox(message = "아직 지원자가 없어요.") }
        }
        items(items = board.applicants, key = { it.id }) { applicant ->
            ApplicantCard(
                applicant = applicant,
                onClick = { onApplicantClick(board.posting.id, applicant.id) },
            )
        }
    }
}

@Composable
private fun PostingHeader(board: ApplicantBoard) {
    val posting = board.posting
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (posting.isUrgent) {
                UrgentBadge()
                Spacer(Modifier.width(6.dp))
            }
            Text(text = board.categoryName, color = AppColors.TextSub, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(text = posting.workSummary, style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${posting.workDate} · ${posting.workTime}",
                color = AppColors.TextSub,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "시급 ${OwnerFormat.won(posting.hourlyWage)}",
                color = AppTheme.role.accent,
                fontSize = 14.sp,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (board.matchedApplicantId != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = "매칭이 확정된 공고예요.", color = AppColors.Success, fontSize = 12.sp)
        }
    }
}
