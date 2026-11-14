package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.owner.ui.component.formatSettlementAmount
import com.workernotfound.app.feature.worker.domain.model.NoShowRiskLevel
import com.workernotfound.app.feature.worker.domain.model.Review
import com.workernotfound.app.feature.worker.domain.model.TrustSummary
import com.workernotfound.app.feature.worker.domain.model.WorkHistoryItem
import com.workernotfound.app.feature.worker.viewmodel.WorkerTrustViewModel

/** 나의 신뢰도 (UI spec 4-2): 게이지 → 지표 → 노쇼 위험도 → 최근 리뷰 → 최근 근무 이력. */
@Composable
fun WorkerTrustScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerTrustViewModel = hiltViewModel(),
    onReviews: () -> Unit = {},
    onHistory: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = uiState.summary
    Column(modifier.fillMaxSize().background(AppColors.WorkerSurface).statusBarsPadding()) {
        AppTopBar(title = stringResource(R.string.trust_title), onBack = onBack)
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.role.accent)
            }
            summary == null -> Column(Modifier.fillMaxSize().padding(AppDimens.screenPadding), verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.trust_load_error), color = AppColors.TextSub)
                uiState.errorMessage?.let { Text(it, fontSize = 12.sp, color = AppColors.TextSub) }
                com.workernotfound.app.core.designsystem.component.AppSecondaryButton(stringResource(R.string.worker_retry), viewModel::load)
            }
            else -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppDimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
            ) {
                TrustGaugeCard(summary)
                RiskBanner(summary.noShowRisk)
                RecentReviewsCard(summary.recentReviews, onReviews)
                RecentHistoryCard(summary.workHistory, onHistory)
            }
        }
    }
}

@Composable
private fun TrustGaugeCard(summary: TrustSummary) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SemicircleGauge(score = summary.score, grade = summary.grade)
        if (summary.score == null) {
            Text(
                stringResource(R.string.trust_score_unavailable),
                fontSize = 12.sp,
                color = AppColors.TextSub,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        val finished = summary.finishedCount.coerceAtLeast(1).toFloat()
        MetricRow(
            stringResource(R.string.trust_total_work),
            stringResource(R.string.trust_count, summary.totalWorkCount),
            summary.totalWorkCount / finished,
            AppTheme.role.accent,
        )
        Spacer(Modifier.height(10.dp))
        MetricRow(
            stringResource(R.string.trust_no_show),
            stringResource(R.string.trust_count, summary.noShowCount),
            summary.noShowCount / finished,
            AppColors.Danger,
        )
        Spacer(Modifier.height(10.dp))
        MetricRow(
            stringResource(R.string.trust_review_average),
            summary.averageRating?.let { "%.1f".format(it) } ?: "-",
            ((summary.averageRating ?: 0.0) / 5.0).toFloat(),
            AppColors.Worker,
        )
    }
}

@Composable
private fun SemicircleGauge(score: Int?, grade: String?) {
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f), contentAlignment = Alignment.BottomCenter) {
        val trackColor = AppColors.Border
        val fillColor = AppTheme.role.accent
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 20.dp.toPx()
            val d = size.width - strokeW
            val topLeft = Offset(strokeW / 2f, strokeW / 2f)
            val arcSize = Size(d, d)
            drawArc(trackColor, 180f, 180f, false, topLeft, arcSize, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            if (score != null) {
                drawArc(
                    fillColor, 180f, 180f * (score / 100f).coerceIn(0f, 1f), false, topLeft, arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 4.dp)) {
            Text(text = score?.toString() ?: "-", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = AppTheme.role.accentPressed)
            Spacer(Modifier.height(4.dp))
            Text(
                text = grade ?: stringResource(R.string.worker_mypage_trust_unavailable),
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
        Text(text = label, fontSize = 13.sp, color = AppColors.TextSub, modifier = Modifier.width(72.dp))
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
            modifier = Modifier.width(56.dp).padding(start = 8.dp),
        )
    }
}

@Composable
private fun RiskBanner(risk: NoShowRiskLevel?) {
    val style = when (risk) {
        NoShowRiskLevel.LOW -> RiskStyle(AppColors.SuccessBg, AppColors.Success, "LOW · 낮음", "성실하게 근무하고 있어요!")
        NoShowRiskLevel.MIDDLE -> RiskStyle(AppColors.WarningBg, AppColors.Warning, "MIDDLE · 주의", "노쇼 없이 근무를 이어가 보세요.")
        NoShowRiskLevel.HIGH -> RiskStyle(AppColors.DangerBg, AppColors.Danger, "HIGH · 높음", "노쇼가 반복되면 매칭이 제한돼요.")
        null -> RiskStyle(AppColors.Border, AppColors.TextSub, "-", stringResource(R.string.trust_risk_unavailable))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.bg, RoundedCornerShape(AppRadius.card))
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "${stringResource(R.string.trust_risk_title)}  ${style.label}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = style.color,
            )
            Spacer(Modifier.height(2.dp))
            Text(text = style.desc, fontSize = 12.sp, color = AppColors.TextSub)
        }
    }
}

private data class RiskStyle(val bg: Color, val color: Color, val label: String, val desc: String)

@Composable
private fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel(text = title, modifier = Modifier.weight(1f))
        com.workernotfound.app.core.designsystem.component.AppSmallButton(stringResource(R.string.trust_view_all).removeSuffix(" ›"), onViewAll)
    }
}

@Composable
private fun RecentReviewsCard(reviews: List<Review>, onViewAll: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.trust_recent_reviews), onViewAll)
        if (reviews.isEmpty()) Text(stringResource(R.string.trust_no_reviews), color = AppColors.TextSub, fontSize = 13.sp)
        reviews.forEachIndexed { index, review ->
            if (index > 0) HorizontalDivider(color = AppColors.Border, modifier = Modifier.padding(vertical = 10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.storeName ?: stringResource(R.string.trust_unknown_store),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain,
                    modifier = Modifier.weight(1f),
                )
                Text(text = stars(review.rating), color = AppColors.Worker, fontSize = 13.sp)
            }
            review.comment?.let {
                Spacer(Modifier.height(4.dp))
                Text(text = it, fontSize = 13.sp, color = AppColors.TextMain)
            }
            Spacer(Modifier.height(4.dp))
            Text(text = review.dateText, fontSize = 11.sp, color = AppColors.TextPlaceholder)
        }
    }
}

@Composable
private fun RecentHistoryCard(history: List<WorkHistoryItem>, onViewAll: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.trust_recent_history), onViewAll)
        if (history.isEmpty()) Text(stringResource(R.string.trust_no_history), color = AppColors.TextSub, fontSize = 13.sp)
        history.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = AppColors.Border, modifier = Modifier.padding(vertical = 10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.storeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${item.dateText} · ${item.rating?.let(::stars) ?: stringResource(R.string.trust_no_rating)}",
                        fontSize = 12.sp,
                        color = AppColors.TextSub,
                    )
                }
                Text(
                    text = item.settlementAmount?.let { formatSettlementAmount(it, item.settlementCurrency ?: "KRW") }
                        ?: stringResource(R.string.trust_settlement_pending),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.settlementAmount != null) AppTheme.role.accentPressed else AppColors.TextSub,
                )
            }
        }
    }
}

private fun stars(rating: Int): String = "★".repeat(rating.coerceIn(0, 5)) + "☆".repeat(5 - rating.coerceIn(0, 5))
