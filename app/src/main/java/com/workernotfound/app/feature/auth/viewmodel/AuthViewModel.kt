package com.workernotfound.app.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.R
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.auth.domain.*
import com.workernotfound.app.feature.job.domain.repository.JobPublicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository, private val registration: RegistrationRepository, private val backend: BackendAccess,
    private val catalog: JobPublicationRepository,
) : ViewModel() {
    private val state = MutableStateFlow(AuthUiState())
    val uiState = state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                state.update { it.copy(emailCooldown = (it.emailCooldown - 1).coerceAtLeast(0), smsCooldown = (it.smsCooldown - 1).coerceAtLeast(0)) }
            }
        }
    }
    fun refreshSession() {
        if (state.value.busy) return
        state.update { it.copy(checkingSession = true, fields = emptyMap(), session = null) }
        runAction {
            val session = auth.currentSession()
            state.update { it.copy(session = session, checkingSession = false) }
        }
    }
    fun edit(field: AuthField, value: String) {
        if (state.value.busy) return
        state.update { it.copy(fields = withValidEnd(it.fields + (field to value), field), message = null, error = null, diagnostic = null,
            emailVerified = if (field == AuthField.EMAIL) false else it.emailVerified,
            smsVerified = if (field == AuthField.PHONE) false else it.smsVerified) }
    }
    /** Available times must end on the same day; drop an end time the new start makes invalid. */
    private fun withValidEnd(fields: Map<AuthField, String>, changed: AuthField): Map<AuthField, String> {
        if (changed != AuthField.START) return fields
        val start = runCatching { java.time.LocalTime.parse(fields[AuthField.START]) }.getOrNull() ?: return fields
        val end = runCatching { java.time.LocalTime.parse(fields[AuthField.END]) }.getOrNull() ?: return fields
        return if (end > start) fields else fields + (AuthField.END to "")
    }
    fun page(page: AuthPage) {
        if (!state.value.busy) state.update { AuthUiState(page = page, checkingSession = false, businessTypes = it.businessTypes) }
        if (page == AuthPage.SIGNUP && state.value.businessTypes.isEmpty()) loadBusinessTypes()
    }
    /** Business types come from the server job catalog so the signup value can be matched when posting jobs. */
    fun loadBusinessTypes() {
        state.update { it.copy(businessTypesFailed = false) }
        viewModelScope.launch {
            try { val types = catalog.categories(); state.update { it.copy(businessTypes = types) } }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state.update { it.copy(businessTypesFailed = true) } }
        }
    }
    fun role(role: String) { if (!state.value.busy) state.update { it.copy(role = role) } }
    fun day(day: DayOfWeek) { if (!state.value.busy) state.update { it.copy(day = day) } }
    fun available(value: Boolean) { if (!state.value.busy) state.update { it.copy(immediatelyAvailable = value) } }
    fun consumedSession() { state.update { AuthUiState(checkingSession = false) } }
    fun send(sms: Boolean) {
        val s = state.value
        if ((sms && (s[AuthField.PHONE].isBlank() || s[AuthField.PHONE].length > 20 || s.smsCooldown > 0)) ||
            (!sms && (!s.emailValid || s.emailCooldown > 0))) return
        runAction {
            registration.sendCode(s[if (sms) AuthField.PHONE else AuthField.EMAIL], sms, s.page == AuthPage.RESET)
            state.update { it.copy(message = if (s.page == AuthPage.RESET) R.string.auth_reset_sent else R.string.auth_code_sent,
                emailVerified = if (!sms) false else it.emailVerified, smsVerified = if (sms) false else it.smsVerified,
                emailCooldown = if (!sms) 60 else it.emailCooldown, smsCooldown = if (sms) 60 else it.smsCooldown) }
        }
    }
    fun verify(sms: Boolean) {
        val s = state.value
        val code = s[if (sms) AuthField.SMS_CODE else AuthField.EMAIL_CODE]
        if (!Regex("[0-9]{6}").matches(code)) return
        runAction {
            val ok = registration.verifyCode(s[if (sms) AuthField.PHONE else AuthField.EMAIL], code, sms)
            state.update { it.copy(message = if (ok) R.string.auth_verified else R.string.auth_invalid_code,
                emailVerified = if (!sms) ok else it.emailVerified, smsVerified = if (sms) ok else it.smsVerified) }
        }
    }
    fun submit() {
        val s = state.value
        when (s.page) {
            AuthPage.LOGIN -> if (s.emailValid && s[AuthField.PASSWORD].isNotBlank()) runAction {
                val session = auth.login(s[AuthField.EMAIL], s[AuthField.PASSWORD])
                state.update { it.copy(session = session, fields = emptyMap()) }
            }
            AuthPage.RESET -> if (s.emailValid && s.passwordValid && Regex("[0-9]{6}").matches(s[AuthField.EMAIL_CODE])) runAction {
                registration.resetPassword(s[AuthField.EMAIL], s[AuthField.EMAIL_CODE], s[AuthField.PASSWORD])
                state.value = AuthUiState(checkingSession = false, message = R.string.auth_reset_done)
            }
            AuthPage.SIGNUP -> if (s.signupValid) runAction {
                val form = SignupForm(s.role, s[AuthField.NAME], s[AuthField.EMAIL], s[AuthField.PASSWORD], s[AuthField.PHONE],
                    s[AuthField.ADDRESS], s[AuthField.LATITUDE].toBigDecimal(), s[AuthField.LONGITUDE].toBigDecimal(),
                    s[AuthField.BUSINESS_NUMBER], s[AuthField.STORE_NAME], s[AuthField.BUSINESS_TYPE],
                    s[AuthField.WAGE].toIntOrNull() ?: 0, s[AuthField.RADIUS].toIntOrNull() ?: 1,
                    s.immediatelyAvailable, s.day.name, s[AuthField.START], s[AuthField.END])
                val session = registration.signup(form)
                state.update { it.copy(session = session, fields = emptyMap()) }
            }
        }
    }
    private fun runAction(action: suspend () -> Unit) {
        if (state.value.busy) return
        state.update { it.copy(busy = true, error = null, message = null, diagnostic = null) }
        viewModelScope.launch {
            try { action() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                val report = backend.diagnostic(e)?.let { "화면: ${state.value.page}\n$it" }
                state.update { it.copy(error = backend.message(e), diagnostic = report, checkingSession = false) }
            }
            finally { state.update { it.copy(busy = false) } }
        }
    }
}
