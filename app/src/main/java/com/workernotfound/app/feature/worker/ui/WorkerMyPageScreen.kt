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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.workernotfound.app.feature.mypage.ui.ProfilePhotoButton
import com.workernotfound.app.feature.mypage.ui.ProfileNameDialog
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
 * 마이페이지 (UI spec 4-1 프로필 설정). 데모용 mock — 실제 인증/계정 연동은 후속.
 * 신뢰 점수 영역을 누르면 '나의 신뢰도'(4-2)로 이동한다.
 */
@Composable
fun WorkerMyPageScreen(
    onTrustClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerMyPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

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

        ProfileCard(profile = profile, onTrustClick = onTrustClick, onPhotoSelected = viewModel::updatePhoto)

        AppCard(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = viewModel::editName, modifier = Modifier.fillMaxWidth()) {
                InfoRow(stringResource(R.string.profile_edit_name), profile.name)
            }
            Spacer(Modifier.height(10.dp)); Divider(color = AppColors.Border); Spacer(Modifier.height(10.dp))
            InfoRow("연락처", profile.phone)
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "알림 설정", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSub)
            Spacer(Modifier.height(8.dp))
            ToggleRow("Push 알림", profile.isPushEnabled) { viewModel.setNotification(WorkerNotificationChannel.PUSH, it) }
            ToggleRow("이메일 알림", profile.isEmailEnabled) { viewModel.setNotification(WorkerNotificationChannel.EMAIL, it) }
            ToggleRow("SMS 알림", profile.isSmsEnabled) { viewModel.setNotification(WorkerNotificationChannel.SMS, it) }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            ActionRow("로그아웃", AppColors.TextMain, onClick = onLogout)
            Spacer(Modifier.height(10.dp)); Divider(color = AppColors.Border); Spacer(Modifier.height(10.dp))
            ActionRow("회원 탈퇴", AppColors.Danger, onClick = viewModel::showWithdrawDialog)
        }
    }

    uiState.nameDraft?.let { draft ->
        ProfileNameDialog(
            name = draft, isValid = uiState.isNameValid, isSaving = false, hasSaveError = false,
            onNameChange = viewModel::changeName, onSave = viewModel::saveName, onDismiss = viewModel::dismissName,
        )
    }
    if (uiState.isWithdrawDialogVisible) {
        AlertDialog(
            onDismissRequest = viewModel::dismissWithdrawDialog,
            containerColor = AppColors.CardSurface,
            title = { Text("회원 탈퇴", fontWeight = FontWeight.Bold, color = AppColors.TextMain) },
            text = { Text("정말 탈퇴하시겠어요? 이 작업은 되돌릴 수 없어요.", color = AppColors.TextSub) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissWithdrawDialog) {
                    Text("탈퇴", color = AppColors.Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissWithdrawDialog) { Text("닫기", color = AppColors.TextSub) }
            },
        )
    }
}

@Composable
private fun ProfileCard(profile: WorkerProfile, onTrustClick: () -> Unit, onPhotoSelected: (String) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).background(AppColors.WorkerLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (profile.profileImageUri != null) AsyncImage(
                    model = profile.profileImageUri, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape),
                ) else Text(text = profile.name.take(1), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.WorkerEmphasis)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = profile.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(2.dp))
                Text(text = "알바생 · ${profile.email}", fontSize = 12.sp, color = AppColors.TextSub)
            }
        }
        ProfilePhotoButton(onSelected = onPhotoSelected)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.WorkerLight, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .clickable(onClick = onTrustClick)
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⭐ 나의 신뢰도", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.WorkerEmphasis)
                Spacer(Modifier.weight(1f))
                Text(text = "92점 · 우수  ›", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.WorkerEmphasis)
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
