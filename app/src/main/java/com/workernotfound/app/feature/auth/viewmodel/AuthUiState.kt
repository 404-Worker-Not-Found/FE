package com.workernotfound.app.feature.auth.viewmodel

import com.workernotfound.app.feature.auth.domain.AuthSession
import java.time.DayOfWeek
import java.time.LocalTime

enum class AuthPage { LOGIN, SIGNUP, RESET }
enum class AuthField {
    EMAIL, PASSWORD, CONFIRM, NAME, PHONE, EMAIL_CODE, SMS_CODE, ADDRESS, LATITUDE, LONGITUDE,
    BUSINESS_NUMBER, STORE_NAME, BUSINESS_TYPE, WAGE, RADIUS, START, END,
}

data class AuthUiState(
    val page: AuthPage = AuthPage.LOGIN,
    val fields: Map<AuthField, String> = emptyMap(),
    val role: String = "WORKER", val day: DayOfWeek = DayOfWeek.MONDAY,
    val immediatelyAvailable: Boolean = false,
    val busy: Boolean = false, val checkingSession: Boolean = true,
    val emailVerified: Boolean = false, val smsVerified: Boolean = false,
    val emailCooldown: Int = 0, val smsCooldown: Int = 0,
    val message: Int? = null, val error: String? = null, val session: AuthSession? = null,
    val diagnostic: String? = null,
) {
    operator fun get(field: AuthField) = fields[field].orEmpty()
    val emailValid get() = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+").matches(this[AuthField.EMAIL]) && this[AuthField.EMAIL].length <= 255
    val passwordValid get() = this[AuthField.PASSWORD].length in 8..100 && this[AuthField.PASSWORD] == this[AuthField.CONFIRM]
    val locationValid get() = this[AuthField.ADDRESS].isNotBlank() && this[AuthField.ADDRESS].length <= 255 &&
        this[AuthField.LATITUDE].toBigDecimalOrNull()?.let { it >= (-90).toBigDecimal() && it <= 90.toBigDecimal() } == true &&
        this[AuthField.LONGITUDE].toBigDecimalOrNull()?.let { it >= (-180).toBigDecimal() && it <= 180.toBigDecimal() } == true
    val signupValid get() = emailValid && passwordValid && emailVerified && smsVerified && locationValid &&
        this[AuthField.NAME].isNotBlank() && this[AuthField.NAME].length <= 50 && this[AuthField.PHONE].isNotBlank() &&
        this[AuthField.PHONE].length <= 20 && this[AuthField.BUSINESS_TYPE].isNotBlank() && this[AuthField.BUSINESS_TYPE].length <= 100 &&
        if (role == "OWNER") Regex("[0-9]{10}").matches(this[AuthField.BUSINESS_NUMBER]) &&
            this[AuthField.STORE_NAME].isNotBlank() && this[AuthField.STORE_NAME].length <= 100
        else (this[AuthField.WAGE].toIntOrNull() ?: -1) >= 0 && (this[AuthField.RADIUS].toIntOrNull() ?: 0) > 0 && validTimes()
    private fun validTimes(): Boolean = try { LocalTime.parse(this[AuthField.END]) > LocalTime.parse(this[AuthField.START]) } catch (_: java.time.format.DateTimeParseException) { false }
    override fun toString() = "AuthUiState(page=$page, busy=$busy)"
}
