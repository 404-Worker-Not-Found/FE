package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.workernotfound.app.feature.mypage.ui.ProfileNameDialog
import com.workernotfound.app.feature.mypage.ui.ProfilePhotoButton
import com.workernotfound.app.feature.worker.viewmodel.WorkerMyPageViewModel
import com.workernotfound.app.feature.worker.viewmodel.WorkerNotificationChannel
import com.workernotfound.app.feature.worker.domain.model.WorkerProfile
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard

/**
 * 마이페이지 (UI spec 4-1 프로필 설정). 계정 변경·탈퇴는 계정 설정 화면에서 처리한다.
 * 신뢰 점수 영역을 누르면 '나의 신뢰도'(4-2)로 이동한다.
 */
@Composable
fun WorkerMyPageScreen(
    onTrustClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerMyPageViewModel = hiltViewModel(),
    onAccountSettings: () -> Unit = {},
    onWithdraw: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    androidx.lifecycle.compose.LifecycleResumeEffect(viewModel) { viewModel.load(); onPauseOrDispose { } }
    LaunchedEffect(uiState.isLoggedOut) { if (uiState.isLoggedOut) onLogout() }
    val profile = uiState.profile
    if (uiState.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppTheme.role.accent) }
        return
    }
    if (profile == null) {
        Column(modifier.fillMaxSize().padding(AppDimens.screenPadding), verticalArrangement = Arrangement.Center) {
            Text(uiState.errorMessage ?: stringResource(R.string.backend_request_failed), color = AppColors.TextSub)
            AppPrimaryButton(text = stringResource(R.string.worker_retry), onClick = viewModel::load)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
    ) {
        Text(
            text = "마이페이지",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        )

        uiState.errorMessage?.let { Text(it, color = AppColors.Danger) }
        ProfileCard(profile = profile, onTrustClick = onTrustClick)

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "알림 설정", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSub)
            Spacer(Modifier.height(8.dp))
            // Push/이메일/SMS 수신 설정 API가 없어 서버 값이 없을 때는 바꿀 수 없는 상태로 표시한다.
            NotificationRow("Push 알림", profile.isPushEnabled) { viewModel.setNotification(WorkerNotificationChannel.PUSH, it) }
            NotificationRow("이메일 알림", profile.isEmailEnabled) { viewModel.setNotification(WorkerNotificationChannel.EMAIL, it) }
            NotificationRow("SMS 알림", profile.isSmsEnabled) { viewModel.setNotification(WorkerNotificationChannel.SMS, it) }
            if (profile.isPushEnabled == null || profile.isEmailEnabled == null || profile.isSmsEnabled == null) {
                Text(stringResource(R.string.worker_mypage_notification_unavailable), color = AppColors.TextSub, fontSize = 12.sp)
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            ActionRow(stringResource(R.string.account_title), AppColors.TextMain, onClick = onAccountSettings)
            Spacer(Modifier.height(10.dp)); Divider(color = AppColors.Border); Spacer(Modifier.height(10.dp))
            ActionRow(stringResource(R.string.profile_logout), AppColors.TextMain, onClick = viewModel::logout)
            Spacer(Modifier.height(10.dp)); Divider(color = AppColors.Border); Spacer(Modifier.height(10.dp))
            ActionRow(stringResource(R.string.worker_mypage_withdraw), AppColors.Danger, onClick = onWithdraw)
        }
    }

    uiState.nameDraft?.let { draft ->
        ProfileNameDialog(
            name = draft, isValid = uiState.isNameValid, isSaving = uiState.isSaving, hasSaveError = uiState.hasNameSaveError,
            onNameChange = viewModel::changeName, onSave = viewModel::saveName, onDismiss = viewModel::dismissName,
        )
    }
}

@Composable
private fun ProfileCard(profile: WorkerProfile, onTrustClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 프로필 사진 저장 API가 없어 상단에는 이니셜 아바타만 둔다.
            Box(
                modifier = Modifier.size(52.dp).background(AppColors.WorkerLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = (profile.name.firstOrNull { it.isLetterOrDigit() } ?: profile.name.firstOrNull() ?: ' ').toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.WorkerEmphasis)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = profile.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(2.dp))
                Text(text = "알바생 · ${profile.email}", fontSize = 12.sp, color = AppColors.TextSub)
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.WorkerLight, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .clickable(onClick = onTrustClick)
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⭐ " + stringResource(R.string.worker_mypage_trust), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.WorkerEmphasis)
                Spacer(Modifier.weight(1f))
                // 신뢰 점수·등급 공개 API가 생기면 worker_mypage_trust_score("{점수}점 · {등급}")로 표시한다.
                Text(
                    text = stringResource(R.string.worker_mypage_trust_unavailable) + "  ›",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.WorkerEmphasis,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 13.sp, color = AppColors.TextSub)
        Spacer(Modifier.weight(1f))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextMain)
    }
}

@Composable
private fun NotificationRow(label: String, checked: Boolean?, onChange: (Boolean) -> Unit) {
    if (checked != null) { ToggleRow(label, checked, onChange); return }
    Row(modifier = Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = AppColors.TextMain)
        Spacer(Modifier.weight(1f))
        Switch(checked = false, onCheckedChange = null, enabled = false)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = AppColors.TextMain)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppTheme.role.accent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = AppColors.Border,
            ),
        )
    }
}

@Composable
private fun ActionRow(label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
        Spacer(Modifier.weight(1f))
        Text(text = "›", fontSize = 16.sp, color = AppColors.TextPlaceholder)
    }
}
