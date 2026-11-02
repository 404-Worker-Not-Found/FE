package com.workernotfound.app.feature.auth.data

import com.workernotfound.app.core.datastore.SessionStore
import com.workernotfound.app.core.datastore.StoredSession
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.auth.data.remote.*
import com.workernotfound.app.feature.auth.domain.*
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegistrationRepositoryImpl @Inject constructor(
    private val api: AuthApiService, private val store: SessionStore,
) : RegistrationRepository {
    override suspend fun sendCode(target: String, sms: Boolean, passwordReset: Boolean) {
        when {
            passwordReset -> api.sendReset(EmailDto(target))
            sms -> api.sendSms(PhoneDto(target))
            else -> api.sendEmail(EmailDto(target))
        }.requireData()
    }
    override suspend fun verifyCode(target: String, code: String, sms: Boolean): Boolean =
        if (sms) api.verifySms(PhoneCodeDto(target, code)).requireData().verified
        else api.verifyEmail(EmailCodeDto(target, code)).requireData().verified

    override suspend fun resetPassword(email: String, code: String, password: String) {
        if (!api.resetPassword(PasswordResetDto(email, code, password)).success) throw ApiException("RESET_FAILED")
    }
    override suspend fun signup(form: SignupForm): AuthSession = withContext(Dispatchers.IO) {
        val location = LocationDto(form.address, form.latitude, form.longitude)
        val result = when (form.role) {
            "OWNER" -> api.signupOwner(OwnerSignupDto(form.name, form.email, form.password, form.phone,
                store.deviceId(), form.businessNumber, form.storeName, form.businessType, location))
            "WORKER" -> api.signupWorker(WorkerSignupDto(form.name, form.email, form.password, form.phone,
                store.deviceId(), form.desiredWage, form.radiusKm, form.immediatelyAvailable, location,
                listOf(form.businessType), listOf(AvailableTimeDto(form.dayOfWeek, form.startTime, form.endTime))))
            else -> throw ApiException("UNSUPPORTED_ROLE")
        }.requireData()
        result.validateSession()
        store.save(StoredSession(result.memberId, result.role, result.tokenResponse.accessToken, result.tokenResponse.refreshToken))
        AuthSession(result.memberId, result.role)
    }
}

internal fun LoginResponseDto.validateSession() {
    if (memberId <= 0 || role !in setOf("OWNER", "WORKER") ||
        tokenResponse.accessToken.isNullOrBlank() || tokenResponse.refreshToken.isNullOrBlank()) {
        throw ApiException("INVALID_SESSION")
    }
}
