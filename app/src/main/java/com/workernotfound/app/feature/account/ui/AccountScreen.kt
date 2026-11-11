package com.workernotfound.app.feature.account.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.feature.account.domain.ContactChannel
import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import com.workernotfound.app.feature.mypage.ui.ProfileNameDialog

/** 계정 정보 관리: 기본 정보 → 연락처 변경 → 회원 탈퇴. Shared by both roles. */
@Composable
fun AccountScreen(onBack: () -> Unit, onLogin: () -> Unit, modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().background(AppTheme.role.screenBackground).statusBarsPadding()) {
        AppTopBar(stringResource(R.string.account_title), onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(AppDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            s.profile?.let { profile ->
                SectionTitle(stringResource(R.string.account_section_info))
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable(enabled = !s.nameSaving, onClick = viewModel::editName)
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.account_name_label), fontSize = 14.sp, color = AppColors.TextSub)
                        Spacer(Modifier.weight(1f))
                        Text(profile.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextMain)
                        Text("  ›", color = AppColors.TextPlaceholder)
                    }
                    HorizontalDivider(color = AppColors.Border, modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.account_contact_label), fontSize = 14.sp, color = AppColors.TextSub)
                        Spacer(Modifier.weight(1f))
                        Text(profile.phone ?: stringResource(R.string.profile_unavailable_field), fontSize = 14.sp,
                            fontWeight = FontWeight.Medium, color = AppColors.TextMain)
                    }
                }
            }

            if (s.key == null) {
                SectionTitle(stringResource(R.string.account_section_change))
                ContactChangeCard(s, viewModel)
                Spacer(Modifier.height(8.dp))
                WithdrawCard(enabled = !s.busy, onWithdraw = { viewModel.confirmWithdrawal(true) })
            } else {
                SectionTitle(stringResource(R.string.account_request_title))
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(when (s.command?.status) {
                        "SUCCEEDED" -> if (s.withdrawing) R.string.account_withdraw_done else R.string.account_change_done
                        "REJECTED" -> R.string.account_rejected
                        else -> R.string.account_pending
                    }), fontSize = 14.sp, lineHeight = 20.sp, color = AppColors.TextMain)
                    Spacer(Modifier.height(12.dp))
                    if (s.command?.terminal != true) {
                        OutlinedButton(viewModel::refresh, enabled = !s.busy, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.account_check), color = AppTheme.role.accentPressed)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    AppPrimaryButton(stringResource(R.string.account_login), onLogin, enabled = !s.busy)
                }
                s.error?.let { Text(it, color = AppColors.Danger, fontSize = 13.sp) }
            }
        }
    }
    s.nameDraft?.let { name ->
        ProfileNameDialog(name, ProfileNamePolicy.isValid(name), s.nameSaving, s.nameError,
            viewModel::name, viewModel::saveName, viewModel::dismissName)
    }
    if (s.confirmWithdrawal) AlertDialog(onDismissRequest = { viewModel.confirmWithdrawal(false) },
        title = { Text(stringResource(R.string.account_withdraw)) }, text = { Text(stringResource(R.string.account_withdraw_confirm)) },
        confirmButton = { TextButton(viewModel::withdraw) { Text(stringResource(R.string.account_withdraw), color = AppColors.Danger) } },
        dismissButton = { TextButton({ viewModel.confirmWithdrawal(false) }) { Text(stringResource(R.string.account_cancel)) } })
}

@Composable
private fun ContactChangeCard(s: AccountUiState, viewModel: AccountViewModel) {
    val isPhone = s.channel == ContactChannel.PHONE
    AppCard(modifier = Modifier.fillMaxWidth()) {
        // 휴대폰 번호 | 이메일 segmented tabs.
        Row(
            Modifier.fillMaxWidth()
                .background(AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(AppRadius.pill))
                .padding(4.dp),
        ) {
            listOf(ContactChannel.PHONE to R.string.account_tab_phone, ContactChannel.EMAIL to R.string.account_tab_email)
                .forEach { (channel, label) ->
                    val selected = s.channel == channel
                    Box(
                        Modifier.weight(1f)
                            .background(if (selected) AppColors.CardSurface else Color.Transparent, RoundedCornerShape(AppRadius.pill))
                            .clickable(enabled = !s.busy) { viewModel.channel(channel) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(label), fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) AppColors.TextMain else AppColors.TextSub,
                        )
                    }
                }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                s.target, viewModel::target,
                placeholder = { Text(stringResource(if (isPhone) R.string.account_new_phone else R.string.account_new_email), fontSize = 14.sp) },
                enabled = !s.busy, singleLine = true, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = if (isPhone) KeyboardType.Phone else KeyboardType.Email),
                shape = RoundedCornerShape(AppRadius.inner),
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = viewModel::send,
                enabled = !s.busy && s.validTarget && s.cooldown == 0,
                shape = RoundedCornerShape(AppRadius.inner),
                border = BorderStroke(1.dp, if (s.validTarget && s.cooldown == 0) AppTheme.role.accent else AppColors.Border),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
            ) {
                Text(
                    if (s.cooldown > 0) stringResource(R.string.account_cooldown, s.cooldown) else stringResource(R.string.account_send),
                    fontSize = 13.sp, color = if (s.validTarget && s.cooldown == 0) AppTheme.role.accentPressed else AppColors.TextSub,
                )
            }
        }
        if (s.sent) {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.account_sent), fontSize = 12.sp, color = AppColors.Success)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            s.code, viewModel::code,
            placeholder = { Text(stringResource(R.string.account_code), fontSize = 14.sp) },
            enabled = !s.busy && s.sent, singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            shape = RoundedCornerShape(AppRadius.inner), isError = s.invalidCode,
        )
        if (s.invalidCode) {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.account_invalid_code), fontSize = 12.sp, color = AppColors.Danger)
        }
        s.error?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, fontSize = 12.sp, color = AppColors.Danger)
        }
        Spacer(Modifier.height(14.dp))
        if (s.busy) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.role.accent, modifier = Modifier.size(28.dp))
            }
        } else {
            AppPrimaryButton(stringResource(R.string.account_change), viewModel::change,
                enabled = s.validTarget && s.sent && Regex("[0-9]{6}").matches(s.code))
        }
        Spacer(Modifier.height(12.dp))
        // 세션 안내: 작은 회색 문단 대신 아이콘이 있는 안내 박스.
        Row(
            Modifier.fillMaxWidth().background(AppTheme.role.accentTint, RoundedCornerShape(AppRadius.inner)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = AppTheme.role.accentPressed, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.account_session_notice_short), fontSize = 13.sp, color = AppColors.TextMain)
        }
    }
}

@Composable
private fun WithdrawCard(enabled: Boolean, onWithdraw: () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .border(1.dp, AppColors.DangerBg, RoundedCornerShape(AppRadius.card))
            .background(AppColors.CardSurface, RoundedCornerShape(AppRadius.card))
            .clickable(enabled = enabled, onClick = onWithdraw)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.account_withdraw), fontSize = 15.sp, fontWeight = FontWeight.Bold,
                color = AppColors.Danger, modifier = Modifier.weight(1f))
            Text("›", fontSize = 16.sp, color = AppColors.Danger)
        }
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.account_withdraw_caption), fontSize = 12.sp, color = AppColors.TextSub)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSub, modifier = Modifier.padding(top = 4.dp))
}
