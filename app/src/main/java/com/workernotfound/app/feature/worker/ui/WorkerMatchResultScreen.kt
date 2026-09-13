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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerApplication
import com.workernotfound.app.feature.worker.viewmodel.RematchOutcome
import com.workernotfound.app.feature.worker.viewmodel.WorkerMatchResultUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerMatchResultViewModel

@Composable
fun WorkerMatchResultScreen(
    onBack: () -> Unit,
    onGoChat: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerMatchResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkerMatchResultContent(
        uiState = uiState,
        onBack = onBack,
        onGoChat = onGoChat,
        onAccept = viewModel::accept,
        onReject = viewModel::reject,
        modifier = modifier,
    )
}

@Composable
private fun WorkerMatchResultContent(
    uiState: WorkerMatchResultUiState,
    onBack: () -> Unit,
    onGoChat: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.WorkerSurface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "매칭 결과", onBack = onBack)

        val app = uiState.application
        when {
            uiState.isLoading -> LoadingBox()
            app == null -> InfoBox("매칭 정보를 불러오지 못했어요.", onBack)
            app.status == ApplicationStatus.MATCHED || uiState.outcome == RematchOutcome.ACCEPTED ->
                MatchSuccessView(app = app, onGoChat = onGoChat, onHome = onBack)
            app.hasRematchOffer && uiState.outcome == RematchOutcome.PENDING ->
                RematchWaitingView(
                    app = app,
                    remainingSeconds = uiState.remainingSeconds,
                    onAccept = onAccept,
                    onReject = onReject,
                )
            uiState.outcome == RematchOutcome.REJECTED -> InfoBox("재매칭을 거절했어요.", onBack)
            uiState.outcome == RematchOutcome.EXPIRED -> InfoBox("응답 시간이 지나 다음 지원자에게 넘어갔어요.", onBack)
            else -> InfoBox("아직 매칭 결과가 없어요.", onBack)
        }
    }
}

@Composable
private fun MatchSuccessView(app: WorkerApplication, onGoChat: () -> Unit, onHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppDimens.screenPadding),
    ) {
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🎉", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = "매칭 성공!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${app.storeName}과 매칭됐어요",
                color = AppColors.TextSub,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(20.dp))
        ScheduleCard(app)
        Spacer(Modifier.height(20.dp))
        AppPrimaryButton(text = "채팅방으로 이동", onClick = onGoChat)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text(text = "홈으로", color = AppColors.TextSub)
        }
    }
}

@Composable
private fun RematchWaitingView(
    app: WorkerApplication,
    remainingSeconds: Long,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppDimens.screenPadding),
    ) {
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🔔", fontSize = 44.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = "재매칭 요청", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "먼저 매칭된 분이 취소되어 기회가 왔어요",
                color = AppColors.TextSub,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(16.dp))
        CountdownBanner(remainingSeconds)
        Spacer(Modifier.height(12.dp))
        ScheduleCard(app, etaMinutes = app.etaMinutes)
        Spacer(Modifier.height(20.dp))
        AppPrimaryButton(text = "수락하기", onClick = onAccept)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onReject, modifier = Modifier.fillMaxWidth()) {
            Text(text = "거절", color = AppColors.TextSub)
        }
    }
}

@Composable
private fun CountdownBanner(remainingSeconds: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.DangerBg, RoundedCornerShape(AppRadius.inner))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "응답 제한 시간", color = AppColors.Danger, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatMinSec(remainingSeconds),
                color = AppColors.Danger,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ScheduleCard(app: WorkerApplication, etaMinutes: Int = 0) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = app.storeName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        Spacer(Modifier.height(2.dp))
        Text(text = app.address, color = AppColors.TextSub, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        InfoLine("근무 날짜", app.workDate)
        Spacer(Modifier.height(6.dp))
        InfoLine("근무 시간", app.workTimeRange)
        Spacer(Modifier.height(6.dp))
        InfoLine("시급", "₩${"%,d".format(app.hourlyWage)}")
        if (etaMinutes > 0) {
            Spacer(Modifier.height(6.dp))
            InfoLine("도착 예상", "${etaMinutes}분")
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = AppColors.TextSub, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(text = value, color = AppColors.TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatMinSec(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.role.accent)
    }
}

@Composable
private fun InfoBox(message: String, onHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(AppDimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, color = AppColors.TextSub, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        AppPrimaryButton(text = "홈으로", onClick = onHome)
    }
}
