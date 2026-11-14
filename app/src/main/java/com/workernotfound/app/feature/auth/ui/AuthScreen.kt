package com.workernotfound.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.feature.auth.domain.AuthSession
import com.workernotfound.app.feature.auth.viewmodel.*
import com.workernotfound.app.feature.job.ui.component.TimePickerField
import java.time.DayOfWeek
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(onAuthenticated: (AuthSession) -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refreshSession() }
    LaunchedEffect(state.session) {
        state.session?.let { viewModel.consumedSession(); onAuthenticated(it) }
    }
    WorkerNotFoundTheme(role = if (state.role == "OWNER") AppRole.OWNER else AppRole.WORKER) {
        AuthContent(state, viewModel::edit, viewModel::page, viewModel::role, viewModel::day,
            viewModel::available, viewModel::send, viewModel::verify, viewModel::submit, viewModel::loadBusinessTypes)
    }
}

@Composable
private fun AuthContent(
    state: AuthUiState, edit: (AuthField, String) -> Unit, page: (AuthPage) -> Unit,
    role: (String) -> Unit, day: (DayOfWeek) -> Unit, available: (Boolean) -> Unit,
    send: (Boolean) -> Unit, verify: (Boolean) -> Unit, submit: () -> Unit,
    onRetryBusinessTypes: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(state.diagnostic) { mutableStateOf(false) }
    if (state.page == AuthPage.LOGIN) {
        LoginContent(state, edit, page, submit,
            copied = copied, onCopy = { report -> clipboard.setText(AnnotatedString(report)); copied = true })
        return
    }
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
            com.workernotfound.app.feature.location.LocationPicker(
                address = state[AuthField.ADDRESS], latitude = state[AuthField.LATITUDE], longitude = state[AuthField.LONGITUDE], enabled = !state.busy,
                onAddress = { edit(AuthField.ADDRESS, it); edit(AuthField.LATITUDE, ""); edit(AuthField.LONGITUDE, "") },
                onPick = { point -> edit(AuthField.ADDRESS, point.address); edit(AuthField.LATITUDE, point.latitude.toString()); edit(AuthField.LONGITUDE, point.longitude.toString()) },
            )
            BusinessTypeField(state, edit, retry = onRetryBusinessTypes)
            if (state.role == "OWNER") {
                listOf(AuthField.BUSINESS_NUMBER, AuthField.STORE_NAME).forEach { AuthInput(it, state, edit) }
                Text(stringResource(R.string.auth_business_verification))
            } else {
                listOf(AuthField.WAGE, AuthField.RADIUS).forEach { AuthInput(it, state, edit) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
                    listOf(AuthField.START, AuthField.END).forEach { field ->
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(fieldLabel(field)), style = MaterialTheme.typography.labelLarge)
                            TimePickerField(state[field].ifBlank { null }, { edit(field, it) },
                                placeholder = stringResource(R.string.time_select_placeholder),
                                startTime = if (field == AuthField.END) state[AuthField.START].ifBlank { null } else null,
                                enabled = !state.busy, sameDayOnly = true)
                        }
                    }
                }
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
        state.diagnostic?.let { report ->
            TextButton(onClick = { clipboard.setText(AnnotatedString(report)); copied = true }) {
                Text(stringResource(if (copied) R.string.auth_error_copied else R.string.auth_copy_error))
            }
            Text(report, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
        }
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

@Composable
private fun BusinessTypeField(state: AuthUiState, edit: (AuthField, String) -> Unit, retry: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = state.businessTypes.firstOrNull { it.key == state[AuthField.BUSINESS_TYPE] }
    Column(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.auth_business_type), style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton({ expanded = true }, enabled = !state.busy && state.businessTypes.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                Text(selected?.name ?: stringResource(if (state.businessTypes.isEmpty() && !state.businessTypesFailed)
                    R.string.auth_business_type_loading else R.string.time_select_placeholder))
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                state.businessTypes.forEach { type ->
                    DropdownMenuItem(text = { Text(type.name) }, onClick = { edit(AuthField.BUSINESS_TYPE, type.key); expanded = false })
                }
            }
        }
        if (state.businessTypesFailed) TextButton(retry, enabled = !state.busy) { Text(stringResource(R.string.auth_business_type_retry)) }
    }
}

/** 로그인: 작은 로고·앱 이름, 입력 두 칸, 로그인·회원가입 버튼만 둔 단순한 구성. */
@Composable
private fun LoginContent(
    state: AuthUiState, edit: (AuthField, String) -> Unit, page: (AuthPage) -> Unit, submit: () -> Unit,
    copied: Boolean, onCopy: (String) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(AppTheme.role.screenBackground).safeDrawingPadding()
            .imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Spacer(Modifier.height(72.dp))
        BrandLogo()
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.app_name), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.auth_login_tagline), fontSize = 14.sp, color = AppColors.TextSub)
        Spacer(Modifier.height(40.dp))
        if (state.checkingSession) {
            CircularProgressIndicator(color = AppTheme.role.accent)
            return@Column
        }
        LoginField(AuthField.EMAIL, state, edit)
        Spacer(Modifier.height(10.dp))
        LoginField(AuthField.PASSWORD, state, edit, onDone = {
            if (state.emailValid && state[AuthField.PASSWORD].isNotBlank() && !state.busy) submit()
        })
        state.message?.let { Spacer(Modifier.height(8.dp)); Text(stringResource(it), fontSize = 13.sp, color = AppColors.TextSub) }
        state.error?.let { Spacer(Modifier.height(8.dp)); Text(it, fontSize = 13.sp, color = AppColors.Danger) }
        state.diagnostic?.let { report ->
            Spacer(Modifier.height(8.dp))
            com.workernotfound.app.core.designsystem.component.AppSmallButton(
                stringResource(if (copied) R.string.auth_error_copied else R.string.auth_copy_error), { onCopy(report) },
                color = AppColors.TextSub)
            Text(report, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
        }
        Spacer(Modifier.height(24.dp))
        if (state.busy) {
            Box(Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.role.accent, modifier = Modifier.size(28.dp))
            }
        } else {
            AppPrimaryButton(stringResource(R.string.auth_login), onClick = submit,
                enabled = state.emailValid && state[AuthField.PASSWORD].isNotBlank())
        }
        Spacer(Modifier.height(10.dp))
        com.workernotfound.app.core.designsystem.component.AppSecondaryButton(
            stringResource(R.string.auth_signup), { page(AuthPage.SIGNUP) }, enabled = !state.busy)
        Spacer(Modifier.height(8.dp))
        // 비밀번호 재설정은 테두리 없이 조용한 보조 동작으로 둔다.
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(onClick = { page(AuthPage.RESET) }, enabled = !state.busy) {
                Text(stringResource(R.string.auth_reset), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextSub)
            }
        }
    }
}

/** 앱 아이콘과 같은 주황 원 + 번개. */
@Composable
private fun BrandLogo() {
    Box(Modifier.size(56.dp).background(AppColors.Primary, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Icon(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, tint = Color.White,
            modifier = Modifier.size(56.dp))
    }
}

@Composable
private fun LoginField(
    field: AuthField, state: AuthUiState, edit: (AuthField, String) -> Unit, onDone: (() -> Unit)? = null,
) {
    val secret = field == AuthField.PASSWORD
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = state[field], onValueChange = { edit(field, it) }, modifier = Modifier.fillMaxWidth(),
        enabled = !state.busy, singleLine = true,
        placeholder = { Text(stringResource(fieldLabel(field)), color = AppColors.TextPlaceholder) },
        trailingIcon = if (secret) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(if (visible) R.string.auth_password_hide else R.string.auth_password_show),
                        tint = AppColors.TextSub)
                }
            }
        } else null,
        visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (secret) KeyboardType.Password else KeyboardType.Email,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTheme.role.accent, unfocusedBorderColor = AppColors.Border,
            focusedContainerColor = AppColors.CardSurface, unfocusedContainerColor = AppColors.CardSurface,
            cursorColor = AppTheme.role.accent,
        ),
    )
}
