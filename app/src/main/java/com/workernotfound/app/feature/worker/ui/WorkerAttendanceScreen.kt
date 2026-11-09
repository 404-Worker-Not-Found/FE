package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.component.AppTopBar

/** GPS 출근 체크 (UI spec 3-6), opened from 근무 관리 separately from 나의 신뢰도. */
@Composable
fun WorkerAttendanceScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(AppColors.WorkerSurface).statusBarsPadding()) {
        AppTopBar(title = stringResource(R.string.worker_work_attendance), onBack = onBack)
        WorkerAttendancePanel(modifier = Modifier.weight(1f))
    }
}
