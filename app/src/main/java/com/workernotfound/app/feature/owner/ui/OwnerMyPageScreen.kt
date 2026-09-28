package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.feature.owner.domain.model.NotificationSettings
import com.workernotfound.app.feature.owner.domain.model.OwnerProfile
import com.workernotfound.app.feature.owner.ui.component.OwnerDialog
import com.workernotfound.app.feature.owner.ui.component.OwnerInfoLine
import com.workernotfound.app.feature.owner.ui.component.OwnerLoadingBox
import com.workernotfound.app.feature.owner.ui.component.OwnerMessageBox
import com.workernotfound.app.feature.owner.ui.component.ProfileAvatar
import com.workernotfound.app.feature.owner.viewmodel.NotificationChannel
import com.workernotfound.app.feature.owner.viewmodel.OwnerMyPageUiState
import com.workernotfound.app.feature.owner.viewmodel.OwnerMyPageViewModel

/**
 * 점주 마이페이지 (UI spec 4-1 프로필 설정, owner fields). Mirrors the worker
 * 마이페이지 layout; there is no owner trust screen, so the 신뢰 점수 link is omitted.
 */
@Composable
fun OwnerMyPageScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerMyPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OwnerMyPageContent(
        uiState = uiState,
        onRetry = viewModel::load,
        onToggle = viewModel::setNotification,
        onLogout = onLogout,
        onWithdraw = viewModel::showWithdrawDialog,
        modifier = modifier,
    )
    if (uiState.isWithdrawDialogVisible) {
        OwnerDialog(
            title = "회원 탈퇴",
            message = "정말 탈퇴하시겠어요? 이 작업은 되돌릴 수 없어요.",
            confirmText = "탈퇴",
            onConfirm = viewModel::dismissWithdrawDialog,
            dismissText = "닫기",
            onDismiss = viewModel::dismissWithdrawDialog,
            isDestructive = true,
        )
    }
}

@Composable
private fun OwnerMyPageContent(
    uiState: OwnerMyPageUiState,
    onRetry: () -> Unit,
    onToggle: (NotificationChannel, Boolean) -> Unit,
    onLogout: () -> Unit,
    onWithdraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = uiState.profile
    when {
        uiState.isLoading -> OwnerLoadingBox(modifier)
        profile == null -> OwnerMessageBox(
            message = uiState.errorMessage ?: "프로필을 불러오지 못했어요.",
            actionText = "다시 시도",
            onAction = onRetry,
            modifier = modifier,
        )
        else -> Column(
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
            ProfileCard(profile)
            AccountCard(profile)
            NotificationCard(settings = profile.notificationSettings, onToggle = onToggle)
            AppCard(modifier = Modifier.fillMaxWidth()) {
                ActionRow(label = "로그아웃", color = AppColors.TextMain, onClick = onLogout)
                CardDivider()
                ActionRow(label = "회원 탈퇴", color = AppColors.Danger, onClick = onWithdraw)
            }
        }
    }
}

@Composable
private fun ProfileCard(profile: OwnerProfile) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(name = profile.name, imageUrl = profile.profileImageUrl, size = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = profile.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(2.dp))
                Text(text = "점주 · ${profile.email}", fontSize = 12.sp, color = AppColors.TextSub)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "🏪 ${profile.storeName}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.role.accent,
        )
    }
}

@Composable
private fun AccountCard(profile: OwnerProfile) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        OwnerInfoLine(label = "이름", value = profile.name)
        CardDivider()
        OwnerInfoLine(label = "연락처", value = profile.phone)
    }
}

@Composable
private fun NotificationCard(settings: NotificationSettings, onToggle: (NotificationChannel, Boolean) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = "알림 설정", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSub)
        Spacer(Modifier.height(8.dp))
        ToggleRow("Push 알림", settings.isPushEnabled) { onToggle(NotificationChannel.PUSH, it) }
        ToggleRow("이메일 알림", settings.isEmailEnabled) { onToggle(NotificationChannel.EMAIL, it) }
        ToggleRow("SMS 알림", settings.isSmsEnabled) { onToggle(NotificationChannel.SMS, it) }
    }
}

@Composable
private fun ToggleRow(label: String, isChecked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = AppColors.TextMain)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.CardSurface,
                checkedTrackColor = AppTheme.role.accent,
                uncheckedThumbColor = AppColors.CardSurface,
                uncheckedTrackColor = AppColors.Border,
                uncheckedBorderColor = AppColors.Border,
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

@Composable
private fun CardDivider() {
    Spacer(Modifier.height(10.dp))
    HorizontalDivider(color = AppColors.Border)
    Spacer(Modifier.height(10.dp))
}
