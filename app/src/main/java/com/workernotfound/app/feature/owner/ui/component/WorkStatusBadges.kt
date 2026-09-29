package com.workernotfound.app.feature.owner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.component.AppBadge
import com.workernotfound.app.core.designsystem.component.AppStatus
import com.workernotfound.app.core.designsystem.component.StatusBadge
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import com.workernotfound.app.feature.owner.viewmodel.WorkTiming

/** 출근 확인 (UI spec 2-4): GPS 기반 출근 인증 인증완료 / 미완료. */
@Composable
internal fun AttendanceBadge(isVerified: Boolean, modifier: Modifier = Modifier) {
    if (isVerified) {
        AppBadge(text = "출근 인증완료", background = AppColors.SuccessBg, contentColor = AppColors.Success, modifier = modifier)
    } else {
        AppBadge(text = "출근 미완료", background = AppColors.DangerBg, contentColor = AppColors.Danger, modifier = modifier)
    }
}

/** Shift progress chip: 시작 전 / 근무중 / 정산 대기 / 노쇼. */
@Composable
internal fun WorkProgressBadge(timing: WorkTiming, modifier: Modifier = Modifier) {
    when {
        timing.work.status == WorkProgressStatus.AWAITING_SETTLEMENT ->
            AppBadge(text = "정산 대기", background = AppColors.WarningBg, contentColor = AppColors.Warning, modifier = modifier)
        timing.work.status == WorkProgressStatus.NO_SHOW -> StatusBadge(status = AppStatus.NO_SHOW, modifier = modifier)
        timing.work.status == WorkProgressStatus.SETTLED -> StatusBadge(status = AppStatus.DONE, modifier = modifier)
        !timing.hasStarted ->
            AppBadge(text = "시작 전", background = AppColors.Border, contentColor = AppColors.TextSub, modifier = modifier)
        else -> StatusBadge(status = AppStatus.WORKING, modifier = modifier)
    }
}
