package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppRadius

/** Generic pill badge used by status chips, urgency, and risk markers. */
@Composable
fun AppBadge(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = contentColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(background, RoundedCornerShape(AppRadius.pill))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** ⚡ 긴급 badge (style guide: 긴급 배지). */
@Composable
fun UrgentBadge(modifier: Modifier = Modifier) {
    AppBadge(
        text = "⚡ 긴급",
        background = AppColors.DangerBg,
        contentColor = AppColors.Danger,
        modifier = modifier,
    )
}

/** Owner posting / application status chip (style guide: 상태 배지). */
enum class AppStatus(val label: String, val background: Color, val contentColor: Color) {
    RECRUITING("모집중", AppColors.SuccessBg, AppColors.Success),
    CLOSED("마감", AppColors.Border, AppColors.TextSub),
    DONE("완료", AppColors.SuccessBg, AppColors.Success),
    MATCHED("매칭완료", AppColors.SuccessBg, AppColors.Success),
    MATCHING("매칭중", AppColors.WorkerLight, AppColors.WorkerEmphasis),
    WORKING("근무중", AppColors.WorkingChipBg, AppColors.WorkingChip),
    NO_SHOW("노쇼", AppColors.DangerBg, AppColors.Danger),
    DISPUTE("분쟁", AppColors.WarningBg, AppColors.Warning),
    APPLYING("지원중", AppColors.WorkerLight, AppColors.WorkerEmphasis),
    NOT_SELECTED("미선정", AppColors.Border, AppColors.TextSub),
}

@Composable
fun StatusBadge(status: AppStatus, modifier: Modifier = Modifier) {
    AppBadge(
        text = status.label,
        background = status.background,
        contentColor = status.contentColor,
        modifier = modifier,
    )
}

/** No-show risk (style guide: 노쇼 위험도 LOW/MIDDLE/HIGH). */
enum class NoShowRisk(val label: String, val background: Color, val contentColor: Color) {
    LOW("낮음", AppColors.SuccessBg, AppColors.Success),
    MIDDLE("주의", AppColors.WarningBg, AppColors.Warning),
    HIGH("높음", AppColors.DangerBg, AppColors.Danger),
}

@Composable
fun NoShowRiskBadge(risk: NoShowRisk, modifier: Modifier = Modifier) {
    AppBadge(
        text = "노쇼 ${risk.label}",
        background = risk.background,
        contentColor = risk.contentColor,
        modifier = modifier,
    )
}
