package com.workernotfound.app.feature.auth.domain

import java.math.BigDecimal

interface RegistrationRepository {
    suspend fun sendCode(target: String, sms: Boolean, passwordReset: Boolean = false)
    suspend fun verifyCode(target: String, code: String, sms: Boolean): Boolean
    suspend fun signup(form: SignupForm): AuthSession
    suspend fun resetPassword(email: String, code: String, password: String)
}

// Plain classes intentionally omit generated toString() for credentials and codes.
class SignupForm(
    val role: String, val name: String, val email: String, val password: String,
    val phone: String, val address: String, val latitude: BigDecimal, val longitude: BigDecimal,
    val businessNumber: String, val storeName: String, val businessType: String,
    val desiredWage: Int, val radiusKm: Int, val immediatelyAvailable: Boolean,
    val dayOfWeek: String, val startTime: String, val endTime: String,
)
