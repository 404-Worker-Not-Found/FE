package com.workernotfound.app.feature.owner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.NoShowRiskBadge
import com.workernotfound.app.feature.owner.domain.model.Applicant
import com.workernotfound.app.feature.owner.ui.component.OwnerInfoLine
import com.workernotfound.app.feature.owner.ui.component.OwnerOutlinedButton
import com.workernotfound.app.feature.owner.ui.component.ProfileAvatar
import com.workernotfound.app.feature.owner.ui.component.toBadge
import com.workernotfound.app.feature.owner.viewmodel.OwnerRematchUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerRematchViewModel
import com.workernotfound.app.feature.owner.viewmodel.RematchPhase

/** 노쇼 처리 (UI spec 2-5): 자동 재매칭 안내 → 재매칭 성공 / 실패. */
@Composable
fun OwnerRematchScreen(
    onClose: () -> Unit,
    onViewWork: (workId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerRematchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // The no-show is already confirmed; leaving always returns to the owner root.
    BackHandler(onBack = onClose)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "노쇼 처리", onBack = onClose)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
        ) {
            NoShowNotice(uiState)
            when (uiState.phase) {
                RematchPhase.SEARCHING -> SearchingView()
                RematchPhase.SUCCESS -> SuccessView(uiState, onViewWork = onViewWork, onClose = onClose)
                RematchPhase.FAILURE -> FailureView(
                    uiState = uiState,
                    onReopen = viewModel::reopenPosting,
                    onClosePosting = viewModel::closePosting,
                )
                RematchPhase.REOPENED -> ResolvedView("공고를 다시 열었어요.\n새 지원자가 오면 지원자 관리에서 확인할 수 있어요.", onClose)
                RematchPhase.CLOSED -> ResolvedView("공고를 마감했어요.\n지난 공고 관리에서 노쇼 내역을 확인할 수 있어요.", onClose)
                RematchPhase.ERROR -> ErrorView(uiState.errorMessage, onRetry = viewModel::startRematch)
            }
        }
    }
}

/** 자동 재매칭 안내 메시지 (UI spec 2-5). */
@Composable
private fun NoShowNotice(uiState: OwnerRematchUiState) {
    val name = uiState.noShowWork?.workerName ?: "알바생"
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = "노쇼가 확정됐어요", color = AppColors.Danger, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$name 님을 노쇼 처리했어요. 같은 공고의 다른 지원자에게 자동으로 재매칭을 진행해요.",
            color = AppColors.TextSub,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun SearchingView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = AppTheme.role.accent)
        Spacer(Modifier.height(16.dp))
        Text(text = "자동 재매칭 중...", color = AppColors.TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(text = "조건에 맞는 지원자를 찾고 있어요.", color = AppColors.TextSub, fontSize = 13.sp)
    }
}

/** 재매칭 성공: 새 매칭 상대 정보 및 도착 예정 시간. */
@Composable
private fun SuccessView(uiState: OwnerRematchUiState, onViewWork: (String) -> Unit, onClose: () -> Unit) {
    val applicant = uiState.newApplicant ?: return
    val newWork = uiState.newWork ?: return
    ResultTitle(emoji = "🎉", title = "재매칭 성공", subtitle = "새로운 알바생이 매칭됐어요.")
    NewMatchCard(applicant = applicant, arrivalMillis = uiState.expectedArrivalMillis)
    AppPrimaryButton(text = "근무 현황 보기", onClick = { onViewWork(newWork.id) })
    TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
        Text(text = "확인", color = AppColors.TextSub)
    }
}

@Composable
private fun NewMatchCard(applicant: Applicant, arrivalMillis: Long?) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(name = applicant.name, imageUrl = applicant.profileImageUrl)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = applicant.name, color = AppColors.TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${applicant.age}세 · 평점 %.1f · 신뢰도 ${applicant.trustScore}점".format(applicant.rating),
                    color = AppColors.TextSub,
                    fontSize = 12.sp,
                )
            }
            NoShowRiskBadge(risk = applicant.noShowRisk.toBadge())
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = AppColors.Border)
        Spacer(Modifier.height(12.dp))
        OwnerInfoLine(label = "매칭 점수", value = "${applicant.matchScore}점")
        Spacer(Modifier.height(8.dp))
        OwnerInfoLine(
            label = "도착 예정 시간",
            value = arrivalText(applicant.etaMinutes, arrivalMillis),
            valueColor = AppTheme.role.accent,
            isEmphasized = true,
        )
    }
}

private fun arrivalText(etaMinutes: Int, arrivalMillis: Long?): String =
    if (arrivalMillis == null) "${etaMinutes}분 후" else "${etaMinutes}분 후 (${OwnerFormat.clock(arrivalMillis)})"

/** 재매칭 실패: 공고 재오픈 또는 마감 선택. */
@Composable
private fun FailureView(uiState: OwnerRematchUiState, onReopen: () -> Unit, onClosePosting: () -> Unit) {
    ResultTitle(
        emoji = "😥",
        title = "재매칭 실패",
        subtitle = "조건에 맞는 지원자가 없어요.\n공고를 다시 열어 모집하거나 마감할 수 있어요.",
    )
    uiState.errorMessage?.let { Text(text = it, color = AppColors.Danger, fontSize = 13.sp) }
    AppPrimaryButton(text = "공고 재오픈", onClick = onReopen, enabled = !uiState.isProcessing)
    OwnerOutlinedButton(
        text = "마감",
        onClick = onClosePosting,
        enabled = !uiState.isProcessing,
        color = AppColors.TextSub,
    )
}

@Composable
private fun ResolvedView(message: String, onClose: () -> Unit) {
    ResultTitle(emoji = "✅", title = "처리 완료", subtitle = message)
    AppPrimaryButton(text = "확인", onClick = onClose)
}

@Composable
private fun ErrorView(message: String?, onRetry: () -> Unit) {
    ResultTitle(emoji = "⚠️", title = "재매칭 오류", subtitle = message ?: "재매칭을 진행하지 못했어요.")
    AppPrimaryButton(text = "다시 시도", onClick = onRetry)
}

@Composable
private fun ResultTitle(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = emoji, fontSize = 44.sp)
        Spacer(Modifier.height(8.dp))
        Text(text = title, color = AppColors.TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle, color = AppColors.TextSub, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
