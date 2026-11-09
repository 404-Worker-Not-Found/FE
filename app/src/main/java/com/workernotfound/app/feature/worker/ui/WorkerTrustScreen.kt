package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.feature.worker.domain.model.NoShowRiskLevel
import com.workernotfound.app.feature.worker.domain.model.TrustSummary
import com.workernotfound.app.feature.worker.viewmodel.WorkerTrustUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerTrustViewModel

/** 나의 신뢰도 (UI spec 4-2) + 출근 인증. Reviews and work history open their own screens. */
@Composable
fun WorkerTrustScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerTrustViewModel = hiltViewModel(),
    onReviews: () -> Unit = {},
    onHistory: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().background(AppColors.WorkerSurface).statusBarsPadding()) {
        AppTopBar(title = stringResource(R.string.attendance_title), onBack = onBack)
        Row {
            TextButton(onHistory) { Text(stringResource(R.string.history_title)) }
            TextButton(onReviews) { Text(stringResource(R.string.reviews_title)) }
        }
        TrustSummarySection(uiState, onRetry = viewModel::load)
        WorkerAttendancePanel(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TrustSummarySection(uiState: WorkerTrustUiState, onRetry: () -> Unit) {
    val summary = uiState.summary
    Column(Modifier.padding(horizontal = AppDimens.screenPadding)) {
        when {
            uiState.isLoading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.role.accent)
            }
            summary == null -> AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.worker_trust_load_error), color = AppColors.TextSub)
                uiState.errorMessage?.let { Text(it, fontSize = 12.sp, color = AppColors.TextSub) }
                TextButton(onClick = onRetry) { Text(stringResource(R.string.worker_retry)) }
            }
            else -> {
                TrustGaugeCard(summary)
                Spacer(Modifier.height(AppDimens.cardGap))
                RiskBanner(summary.noShowRisk)
            }
        }
    }
}

@Composable
private fun TrustGaugeCard(summary: TrustSummary) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SemicircleGauge(score = summary.score, grade = summary.grade)
        Spacer(Modifier.height(16.dp))
        MetricRow("총 근무", "${summary.totalWorkCount}회", (summary.totalWorkCount / 60f), AppTheme.role.accent)
        Spacer(Modifier.height(10.dp))
        MetricRow("노쇼", "${summary.noShowCount}회", 1f - (summary.noShowCount / 10f), AppColors.Success)
        Spacer(Modifier.height(10.dp))
        MetricRow("리뷰 평점", "%.1f".format(summary.averageRating), (summary.averageRating / 5f).toFloat(), AppColors.Worker)
    }
}

@Composable
private fun SemicircleGauge(score: Int, grade: String) {
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f), contentAlignment = Alignment.BottomCenter) {
        val trackColor = AppColors.Border
        val fillColor = AppTheme.role.accent
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 20.dp.toPx()
            val d = size.width - strokeW
            val topLeft = Offset(strokeW / 2f, strokeW / 2f)
            val arcSize = Size(d, d)
            drawArc(
                color = trackColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round),
            )
            drawArc(
                color = fillColor,
                startAngle = 180f,
                sweepAngle = 180f * (score / 100f).coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 4.dp)) {
            Text(text = "$score", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = AppTheme.role.accentPressed)
            Spacer(Modifier.height(4.dp))
            Text(
                text = grade,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.WorkerEmphasis,
                modifier = Modifier
                    .background(AppColors.WorkerLight, RoundedCornerShape(AppRadius.pill))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, valueText: String, fraction: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 13.sp, color = AppColors.TextSub, modifier = Modifier.width(64.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(AppColors.Border, RoundedCornerShape(AppRadius.pill)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(color, RoundedCornerShape(AppRadius.pill)),
            )
        }
        Text(
            text = valueText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
            modifier = Modifier.width(48.dp),
        )
    }
}

@Composable
private fun RiskBanner(risk: NoShowRiskLevel) {
    val (bg, color, label, desc) = when (risk) {
        NoShowRiskLevel.LOW -> RiskStyle(AppColors.SuccessBg, AppColors.Success, "LOW · 낮음", "성실하게 근무하고 있어요!")
        NoShowRiskLevel.MIDDLE -> RiskStyle(AppColors.WarningBg, AppColors.Warning, "MIDDLE · 주의", "노쇼 없이 근무를 이어가 보세요.")
        NoShowRiskLevel.HIGH -> RiskStyle(AppColors.DangerBg, AppColors.Danger, "HIGH · 높음", "노쇼가 반복되면 매칭이 제한돼요.")
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(AppRadius.card))
            .padding(16.dp),
    ) {
        Column {
            Text(text = "노쇼 위험도  $label", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(2.dp))
            Text(text = desc, fontSize = 12.sp, color = AppColors.TextSub)
        }
    }
}

private data class RiskStyle(val bg: Color, val color: Color, val label: String, val desc: String)
