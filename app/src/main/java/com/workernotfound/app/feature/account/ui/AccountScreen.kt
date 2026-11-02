package com.workernotfound.app.feature.account.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.feature.account.domain.ContactChannel

@Composable
fun AccountScreen(onBack: () -> Unit, onLogin: () -> Unit, modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        AppTopBar(stringResource(R.string.account_title), onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(AppDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            Text(stringResource(R.string.account_session_notice), color = AppColors.TextSub)
            if (s.key == null) {
                Row {
                    ContactChannel.entries.forEach { channel -> TextButton({ viewModel.channel(channel) }, enabled = !s.busy) {
                        Text(stringResource(if (channel == ContactChannel.PHONE) R.string.account_phone else R.string.account_email))
                    } }
                }
                Text(stringResource(if (s.channel == ContactChannel.PHONE) R.string.account_phone else R.string.account_email))
                OutlinedTextField(s.target, viewModel::target, label = { Text(stringResource(R.string.account_new_contact)) }, enabled = !s.busy, singleLine = true, modifier = Modifier.fillMaxWidth())
                TextButton(viewModel::send, enabled = !s.busy && s.validTarget && s.cooldown == 0) {
                    Text(if (s.cooldown > 0) stringResource(R.string.account_cooldown, s.cooldown) else stringResource(R.string.account_send))
                }
                if (s.sent) Text(stringResource(R.string.account_sent))
                OutlinedTextField(s.code, viewModel::code, label = { Text(stringResource(R.string.account_code)) }, enabled = !s.busy, singleLine = true, modifier = Modifier.fillMaxWidth())
                AppPrimaryButton(stringResource(R.string.account_change), viewModel::change,
                    enabled = !s.busy && s.validTarget && s.sent && Regex("[0-9]{6}").matches(s.code))
                HorizontalDivider()
                TextButton({ viewModel.confirmWithdrawal(true) }, enabled = !s.busy) { Text(stringResource(R.string.account_withdraw), color = AppColors.Danger) }
            } else {
                Text(stringResource(when (s.command?.status) {
                    "SUCCEEDED" -> if (s.withdrawing) R.string.account_withdraw_done else R.string.account_change_done
                    "REJECTED" -> R.string.account_rejected
                    else -> R.string.account_pending
                }))
                if (s.command?.terminal != true) TextButton(viewModel::refresh, enabled = !s.busy) { Text(stringResource(R.string.account_check)) }
                TextButton(onLogin, enabled = !s.busy) { Text(stringResource(R.string.account_login)) }
            }
            if (s.invalidCode) Text(stringResource(R.string.account_invalid_code), color = AppColors.Danger)
            if (s.busy) CircularProgressIndicator()
            s.error?.let { Text(it, color = AppColors.Danger) }
        }
    }
    if (s.confirmWithdrawal) AlertDialog(onDismissRequest = { viewModel.confirmWithdrawal(false) },
        title = { Text(stringResource(R.string.account_withdraw)) }, text = { Text(stringResource(R.string.account_withdraw_confirm)) },
        confirmButton = { TextButton(viewModel::withdraw) { Text(stringResource(R.string.account_withdraw), color = AppColors.Danger) } },
        dismissButton = { TextButton({ viewModel.confirmWithdrawal(false) }) { Text(stringResource(R.string.account_cancel)) } })
}
