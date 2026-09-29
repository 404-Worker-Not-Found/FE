package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.NoShowRiskBadge
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.owner.domain.model.ApplicantDetail
import com.workernotfound.app.feature.owner.domain.model.ApplicantWorkRecord
import com.workernotfound.app.feature.owner.domain.model.RECENT_WORK_LIMIT
import com.workernotfound.app.feature.owner.ui.component.ExperienceMark
import com.workernotfound.app.feature.owner.ui.component.OwnerDialog
import com.workernotfound.app.feature.owner.ui.component.OwnerInfoLine
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.ui.component.ProfileAvatar
import com.workernotfound.app.feature.owner.ui.component.toBadge
import com.workernotfound.app.feature.owner.viewmodel.OwnerApplicantDetailUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerApplicantDetailViewModel

/** 지원자 상세 + 매칭 확정 (UI spec 2-3). */
@Composable
fun OwnerApplicantDetailScreen(
    onBack: () -> Unit,
    onViewWork: (workId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerApplicantDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OwnerApplicantDetailContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onRequestConfirm = viewModel::requestConfirm,
        modifier = modifier,
    )
    ApplicantDetailDialogs(
        uiState = uiState,
        onConfirm = viewModel::confirmMatch,
        onDismissConfirm = viewModel::dismissConfirm,
        onDismissError = viewModel::dismissActionError,
        onViewWork = onViewWork,
        onClose = onBack,
    )
}

@Composable
private fun OwnerApplicantDetailContent(
    uiState: OwnerApplicantDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRequestConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "지원자 상세", onBack = onBack)
        val detail = uiState.detail
        when {
            uiState.isLoading -> OwnerLoadingBox()
            detail == null -> OwnerMessageBox(
                message = uiState.errorMessage ?: "지원자 정보를 불러오지 못했어요.",
                actionText = "다시 시도",
                onAction = onRetry,
            )
            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(AppDimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
                ) {
                    ProfileSection(detail)
                    ArrivalSection(detail)
                    WorkHistorySection(detail.recentWorks)
                    NoShowHistorySection(detail)
                }
                ConfirmBar(detail = detail, isConfirming = uiState.isConfirming, onConfirm = onRequestConfirm)
            }
        }
    }
}

@Composable
private fun ProfileSection(detail: ApplicantDetail) {
    val applicant = detail.applicant
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(name = applicant.name, imageUrl = applicant.profileImageUrl, size = 64.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = applicant.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(2.dp))
                Text(text = "${applicant.age}세 · 평점 %.1f".format(applicant.rating), color = AppColors.TextSub, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                NoShowRiskBadge(risk = applicant.noShowRisk.toBadge())
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${applicant.trustScore}점",
                    color = AppTheme.role.accent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = "신뢰도 점수", color = AppColors.TextSub, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = AppColors.Border)
        Spacer(Modifier.height(12.dp))
        OwnerInfoLine(label = "매칭 점수", value = "${applicant.matchScore}점")
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${detail.categoryName} 업종", color = AppColors.TextSub, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            ExperienceMark(hasExperience = applicant.hasCategoryExperience)
        }
    }
}

@Composable
private fun ArrivalSection(detail: ApplicantDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        OwnerInfoLine(
            label = "예상 도착 시간 (현재 위치 기준)",
            value = "${detail.applicant.etaMinutes}분",
            valueColor = AppTheme.role.accent,
            isEmphasized = true,
        )
    }
}

@Composable
private fun WorkHistorySection(works: List<ApplicantWorkRecord>) {
    Column {
        SectionLabel(text = "최근 근무 이력 (최대 ${RECENT_WORK_LIMIT}건)")
        Spacer(Modifier.height(8.dp))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            if (works.isEmpty()) {
                Text(text = "근무 이력이 없어요.", color = AppColors.TextSub, fontSize = 13.sp)
            }
            works.forEachIndexed { index, work ->
                if (index > 0) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = AppColors.Border)
                    Spacer(Modifier.height(10.dp))
                }
                WorkRecordRow(work)
            }
        }
    }
}

@Composable
private fun WorkRecordRow(work: ApplicantWorkRecord) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = work.place, color = AppColors.TextMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = work.dateText, color = AppColors.TextSub, fontSize = 12.sp)
        }
        Text(text = "★".repeat(work.rating), color = AppColors.Warning, fontSize = 13.sp)
    }
}

@Composable
private fun NoShowHistorySection(detail: ApplicantDetail) {
    Column {
        SectionLabel(text = "노쇼 이력")
        Spacer(Modifier.height(8.dp))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            OwnerInfoLine(
                label = "누적 노쇼",
                value = "${detail.noShowCount}회",
                valueColor = if (detail.noShowCount > 0) AppColors.Danger else AppColors.TextMain,
            )
            Spacer(Modifier.height(8.dp))
            OwnerInfoLine(label = "최근 노쇼 일자", value = detail.lastNoShowDate ?: "없음")
        }
    }
}

@Composable
private fun ConfirmBar(detail: ApplicantDetail, isConfirming: Boolean, onConfirm: () -> Unit) {
    val label = when {
        detail.applicant.isMatched -> "매칭 완료"
        !detail.canConfirmMatch -> "매칭할 수 없는 공고예요"
        isConfirming -> "매칭 확정 중..."
        else -> "매칭 확정"
    }
    AppPrimaryButton(
        text = label,
        onClick = onConfirm,
        enabled = detail.canConfirmMatch && !isConfirming,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(AppDimens.screenPadding),
    )
}

@Composable
private fun ApplicantDetailDialogs(
    uiState: OwnerApplicantDetailUiState,
    onConfirm: () -> Unit,
    onDismissConfirm: () -> Unit,
    onDismissError: () -> Unit,
    onViewWork: (String) -> Unit,
    onClose: () -> Unit,
) {
    val name = uiState.detail?.applicant?.name.orEmpty()
    val matchedWorkId = uiState.matchedWorkId
    when {
        uiState.isConfirmDialogVisible -> OwnerDialog(
            title = "매칭 확정",
            message = "${name} 님과 매칭을 확정할까요?\n확정하면 알바생에게 매칭 알림이 발송돼요.",
            confirmText = "확정",
            onConfirm = onConfirm,
            dismissText = "취소",
            onDismiss = onDismissConfirm,
        )
        uiState.actionErrorMessage != null -> OwnerDialog(
            title = "매칭 실패",
            message = uiState.actionErrorMessage,
            confirmText = "확인",
            onConfirm = onDismissError,
        )
        matchedWorkId != null -> OwnerDialog(
            title = "매칭이 확정됐어요",
            message = "${name} 님에게 매칭 알림을 보냈어요.",
            confirmText = "근무 현황 보기",
            onConfirm = { onViewWork(matchedWorkId) },
            dismissText = "닫기",
            onDismiss = onClose,
        )
    }
}
