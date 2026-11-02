package com.workernotfound.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.feature.auth.domain.AuthSession
import com.workernotfound.app.feature.auth.viewmodel.*
import java.time.DayOfWeek

@Composable
fun AuthScreen(onAuthenticated: (AuthSession) -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refreshSession() }
    LaunchedEffect(state.session) {
        state.session?.let { viewModel.consumedSession(); onAuthenticated(it) }
    }
    WorkerNotFoundTheme(role = if (state.role == "OWNER") AppRole.OWNER else AppRole.WORKER) {
        AuthContent(state, viewModel::edit, viewModel::page, viewModel::role, viewModel::day,
            viewModel::available, viewModel::send, viewModel::verify, viewModel::submit)
    }
}

@Composable
private fun AuthContent(
    state: AuthUiState, edit: (AuthField, String) -> Unit, page: (AuthPage) -> Unit,
    role: (String) -> Unit, day: (DayOfWeek) -> Unit, available: (Boolean) -> Unit,
    send: (Boolean) -> Unit, verify: (Boolean) -> Unit, submit: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(AppTheme.role.screenBackground).safeDrawingPadding()
        .imePadding().verticalScroll(rememberScrollState()).padding(AppDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
        Text(stringResource(when (state.page) {
            AuthPage.LOGIN -> R.string.auth_login
            AuthPage.SIGNUP -> R.string.auth_signup
            AuthPage.RESET -> R.string.auth_reset
        }), style = MaterialTheme.typography.titleLarge)
        if (state.checkingSession) { CircularProgressIndicator(); return@Column }
        if (state.page == AuthPage.SIGNUP) {
            Row {
                TextButton(onClick = { role("WORKER") }, enabled = !state.busy) { Text(stringResource(R.string.auth_worker)) }
                TextButton(onClick = { role("OWNER") }, enabled = !state.busy) { Text(stringResource(R.string.auth_owner)) }
            }
            Text(stringResource(if (state.role == "OWNER") R.string.auth_owner else R.string.auth_worker))
            AuthInput(AuthField.NAME, state, edit)
        }
        AuthInput(AuthField.EMAIL, state, edit)
        AuthInput(AuthField.PASSWORD, state, edit)
        if (state.page != AuthPage.LOGIN) {
            AuthInput(AuthField.CONFIRM, state, edit)
            Text(stringResource(R.string.auth_password_rule))
            VerificationFields(false, state, edit, send, verify)
        }
        if (state.page == AuthPage.SIGNUP) {
            Text(stringResource(R.string.auth_verification_rule))
            AuthInput(AuthField.PHONE, state, edit)
            VerificationFields(true, state, edit, send, verify)
            listOf(AuthField.ADDRESS, AuthField.LATITUDE, AuthField.LONGITUDE, AuthField.BUSINESS_TYPE).forEach { AuthInput(it, state, edit) }
            if (state.role == "OWNER") {
                listOf(AuthField.BUSINESS_NUMBER, AuthField.STORE_NAME).forEach { AuthInput(it, state, edit) }
                Text(stringResource(R.string.auth_business_verification))
            } else {
                listOf(AuthField.WAGE, AuthField.RADIUS, AuthField.START, AuthField.END).forEach { AuthInput(it, state, edit) }
                val days = listOf(R.string.auth_mon, R.string.auth_tue, R.string.auth_wed, R.string.auth_thu, R.string.auth_fri, R.string.auth_sat, R.string.auth_sun)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(days[state.day.ordinal]))
                    TextButton(onClick = { day(DayOfWeek.of(state.day.value % 7 + 1)) }, enabled = !state.busy) { Text(stringResource(R.string.auth_next_day)) }
                }
                Row { Checkbox(state.immediatelyAvailable, available, enabled = !state.busy); Text(stringResource(R.string.auth_available)) }
            }
        }
        state.message?.let { Text(stringResource(it)) }
        state.error?.let { Text(it, color = AppColors.Danger) }
        if (state.busy) CircularProgressIndicator()
        AppPrimaryButton(stringResource(when (state.page) {
            AuthPage.LOGIN -> R.string.auth_login
            AuthPage.SIGNUP -> R.string.auth_signup
            AuthPage.RESET -> R.string.auth_reset
        }), onClick = submit, enabled = !state.busy && when (state.page) {
            AuthPage.LOGIN -> state.emailValid && state[AuthField.PASSWORD].isNotBlank()
            AuthPage.SIGNUP -> state.signupValid
            AuthPage.RESET -> state.emailValid && state.passwordValid && Regex("[0-9]{6}").matches(state[AuthField.EMAIL_CODE])
        })
        if (state.page == AuthPage.LOGIN) {
            TextButton(onClick = { page(AuthPage.SIGNUP) }, enabled = !state.busy) { Text(stringResource(R.string.auth_signup)) }
            TextButton(onClick = { page(AuthPage.RESET) }, enabled = !state.busy) { Text(stringResource(R.string.auth_reset)) }
            Text(stringResource(R.string.auth_social_pending), color = AppColors.TextSub)
        } else TextButton(onClick = { page(AuthPage.LOGIN) }, enabled = !state.busy) { Text(stringResource(R.string.auth_back_login)) }
    }
}

@Composable
private fun VerificationFields(sms: Boolean, state: AuthUiState, edit: (AuthField, String) -> Unit, send: (Boolean) -> Unit, verify: (Boolean) -> Unit) {
    val cooldown = if (sms) state.smsCooldown else state.emailCooldown
    val verified = if (sms) state.smsVerified else state.emailVerified
    TextButton(onClick = { send(sms) }, enabled = !state.busy && cooldown == 0 &&
        if (sms) state[AuthField.PHONE].isNotBlank() else state.emailValid) {
        Text(if (cooldown > 0) stringResource(R.string.auth_resend_wait, cooldown) else stringResource(R.string.auth_send_code))
    }
    AuthInput(if (sms) AuthField.SMS_CODE else AuthField.EMAIL_CODE, state, edit)
    if (state.page == AuthPage.SIGNUP) TextButton(onClick = { verify(sms) }, enabled = !state.busy && !verified &&
        Regex("[0-9]{6}").matches(state[if (sms) AuthField.SMS_CODE else AuthField.EMAIL_CODE])) {
        Text(stringResource(if (verified) R.string.auth_verified else R.string.auth_verify))
    }
    Text(stringResource(if (sms) R.string.auth_sms_expiry else R.string.auth_email_expiry), color = AppColors.TextSub)
}

@Composable
private fun AuthInput(field: AuthField, state: AuthUiState, edit: (AuthField, String) -> Unit) {
    val secret = field == AuthField.PASSWORD || field == AuthField.CONFIRM
    OutlinedTextField(state[field], { edit(field, it) }, Modifier.fillMaxWidth(), enabled = !state.busy,
        label = { Text(stringResource(fieldLabel(field))) }, singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = when {
            secret -> KeyboardType.Password
            field == AuthField.EMAIL -> KeyboardType.Email
            field == AuthField.PHONE -> KeyboardType.Phone
            else -> KeyboardType.Text
        }))
}

private fun fieldLabel(field: AuthField): Int = when (field) {
    AuthField.EMAIL -> R.string.auth_email; AuthField.PASSWORD -> R.string.auth_password
    AuthField.CONFIRM -> R.string.auth_confirm; AuthField.NAME -> R.string.auth_name
    AuthField.PHONE -> R.string.auth_phone; AuthField.EMAIL_CODE -> R.string.auth_email_code
    AuthField.SMS_CODE -> R.string.auth_sms_code; AuthField.ADDRESS -> R.string.auth_address
    AuthField.LATITUDE -> R.string.auth_latitude; AuthField.LONGITUDE -> R.string.auth_longitude
    AuthField.BUSINESS_NUMBER -> R.string.auth_business_number; AuthField.STORE_NAME -> R.string.auth_store_name
    AuthField.BUSINESS_TYPE -> R.string.auth_business_type; AuthField.WAGE -> R.string.auth_wage
    AuthField.RADIUS -> R.string.auth_radius; AuthField.START -> R.string.auth_start
    AuthField.END -> R.string.auth_end
}
