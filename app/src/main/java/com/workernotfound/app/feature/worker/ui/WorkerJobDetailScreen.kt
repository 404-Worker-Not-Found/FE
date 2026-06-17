package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.core.designsystem.component.UrgentBadge
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail
import com.workernotfound.app.feature.worker.viewmodel.WorkerJobDetailUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerJobDetailViewModel

@Composable
fun WorkerJobDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerJobDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkerJobDetailContent(
        uiState = uiState,
        onBack = onBack,
        onApply = viewModel::apply,
        onRetry = viewModel::load,
        modifier = modifier,
    )
}

@Composable
private fun WorkerJobDetailContent(
    uiState: WorkerJobDetailUiState,
    onBack: () -> Unit,
    onApply: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.WorkerSurface)
            .statusBarsPadding(),
    ) {
        DetailTopBar(onBack = onBack)
        when {
            uiState.isLoading -> LoadingBox()
            uiState.errorMessage != null -> ErrorBox(uiState.errorMessage, onRetry)
            uiState.detail != null -> LoadedDetail(
                detail = uiState.detail,
                remainingSeconds = uiState.remainingSeconds,
                onApply = onApply,
            )
        }
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = AppColors.TextMain)
        }
        Text(text = "공고 상세", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
    }
}

@Composable
private fun LoadedDetail(
    detail: WorkerJobDetail,
    remainingSeconds: Long,
    onApply: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.screenPadding)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
        ) {
            StoreInfoCard(detail)
            WageBreakdownCard(detail)
            WorkInfoCard(detail, remainingSeconds)
            DescriptionCard(detail)
            ApplicantStatusCard(detail)
        }
        BottomApplyBar(detail = detail, onApply = onApply)
    }
}

@Composable
private fun StoreInfoCard(detail: WorkerJobDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (detail.isUrgent) {
                UrgentBadge()
                Spacer(Modifier.width(8.dp))
            }
            Text(text = detail.category, color = AppColors.TextSub, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(text = detail.storeName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        Spacer(Modifier.height(4.dp))
        Text(text = detail.address, color = AppColors.TextSub, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        MiniMap()
    }
}

/** Small custom mini-map (style guide: 지도 미니맵). Placeholder, not a real map. */
@Composable
private fun MiniMap() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(AppColors.MapBackground, RoundedCornerShape(AppRadius.inner)),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(18.dp).background(AppColors.Primary, CircleShape))
    }
}

@Composable
private fun WageBreakdownCard(detail: WorkerJobDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "시급 정보")
        Spacer(Modifier.height(10.dp))
        WageRow("기본 시급", detail.baseWage, isBonus = false)
        if (detail.urgentBonus > 0) {
            Spacer(Modifier.height(6.dp))
            WageRow("긴급 가산", detail.urgentBonus, isBonus = true)
        }
        if (detail.timeBonus > 0) {
            Spacer(Modifier.height(6.dp))
            WageRow("시간대 가산", detail.timeBonus, isBonus = true)
        }
        Spacer(Modifier.height(10.dp))
        Divider(color = AppColors.Border)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "최종 시급", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
            Spacer(Modifier.weight(1f))
            Text(
                text = "₩${"%,d".format(detail.finalWage)} / 시",
                color = AppTheme.role.accentPressed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun WageRow(label: String, amount: Int, isBonus: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = AppColors.TextSub, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = (if (isBonus) "+" else "") + "${"%,d".format(amount)}원",
            color = AppColors.TextMain,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun WorkInfoCard(detail: WorkerJobDetail, remainingSeconds: Long) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "근무 정보")
        Spacer(Modifier.height(10.dp))
        InfoRow("근무 날짜", detail.workDate)
        Spacer(Modifier.height(6.dp))
        InfoRow("근무 시간", detail.workTimeRange)
        Spacer(Modifier.height(6.dp))
        InfoRow("총 근무시간", detail.totalWorkHours)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.WorkerLight, RoundedCornerShape(AppRadius.inner))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "근무 시작까지", color = AppColors.WorkerEmphasis, fontSize = 12.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatCountdown(remainingSeconds),
                    color = AppColors.WorkerEmphasis,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = AppColors.TextSub, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(text = value, color = AppColors.TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DescriptionCard(detail: WorkerJobDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "업무 내용")
        Spacer(Modifier.height(10.dp))
        Text(text = detail.description, color = AppColors.TextMain, fontSize = 14.sp)
    }
}

@Composable
private fun ApplicantStatusCard(detail: WorkerJobDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "해당 가게 지원 현황")
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatChip(label = "현재 지원자", value = "${detail.applicantCount}명", modifier = Modifier.weight(1f))
            Spacer(Modifier.width(AppDimens.cardGap))
            StatChip(label = "매칭 점수", value = "${detail.matchScore}점", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(AppColors.WorkerLight, RoundedCornerShape(AppRadius.inner))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = value, color = AppColors.WorkerEmphasis, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(text = label, color = AppColors.TextSub, fontSize = 12.sp)
    }
}

@Composable
private fun BottomApplyBar(detail: WorkerJobDetail, onApply: () -> Unit) {
    val applied = detail.applyStatus == ApplyStatus.APPLIED
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardSurface)
            .navigationBarsPadding()
            .padding(AppDimens.screenPadding),
    ) {
        AvailabilityRow(detail = detail)
        Spacer(Modifier.height(10.dp))
        AppPrimaryButton(
            text = when {
                applied -> "지원 완료"
                !detail.withinRadius -> "모집 반경 밖이에요"
                else -> "즉시 지원하기"
            },
            onClick = onApply,
            enabled = !applied && detail.withinRadius,
        )
    }
}

@Composable
private fun AvailabilityRow(detail: WorkerJobDetail) {
    val bg = if (detail.withinRadius) AppColors.SuccessBg else AppColors.DangerBg
    val color = if (detail.withinRadius) AppColors.Success else AppColors.Danger
    val text = if (detail.withinRadius) "지원 가능 · ${detail.radiusText}" else "지원 불가 · ${detail.radiusText}"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(AppRadius.inner))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatCountdown(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
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
