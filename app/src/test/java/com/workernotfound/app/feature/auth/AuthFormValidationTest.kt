package com.workernotfound.app.feature.auth

import com.workernotfound.app.feature.auth.viewmodel.*
import org.junit.Assert.*
import org.junit.Test

class AuthFormValidationTest {
    private fun worker() = AuthUiState(page = AuthPage.SIGNUP, checkingSession = false,
        emailVerified = true, smsVerified = true, fields = mapOf(
            AuthField.EMAIL to "worker@example.com", AuthField.PASSWORD to "synthetic-password",
            AuthField.CONFIRM to "synthetic-password", AuthField.NAME to "근로자", AuthField.PHONE to "01000000000",
            AuthField.ADDRESS to "서울", AuthField.LATITUDE to "37.5", AuthField.LONGITUDE to "127",
            AuthField.BUSINESS_TYPE to "카페", AuthField.WAGE to "10320", AuthField.RADIUS to "3",
            AuthField.START to "09:00", AuthField.END to "12:00"))
    @Test fun requiresBothServerVerificationsAndWorkerPreferences() {
        val valid = worker()
        assertTrue(valid.signupValid)
        assertFalse(valid.copy(emailVerified = false).signupValid)
        assertFalse(valid.copy(smsVerified = false).signupValid)
        listOf(AuthField.WAGE, AuthField.RADIUS, AuthField.START, AuthField.BUSINESS_TYPE).forEach {
            assertFalse(valid.copy(fields = valid.fields - it).signupValid)
        }
        assertFalse(valid.copy(fields = valid.fields + (AuthField.LATITUDE to "91")).signupValid)
        assertFalse(valid.copy(fields = valid.fields + (AuthField.END to "08:00")).signupValid)
    }
    @Test fun ownerDoesNotRequireWorkerFieldsButRequiresBusinessDetails() {
        val owner = worker().copy(role = "OWNER", fields = worker().fields - AuthField.WAGE - AuthField.RADIUS - AuthField.START - AuthField.END +
            mapOf(AuthField.BUSINESS_NUMBER to "0000000000", AuthField.STORE_NAME to "가게"))
        assertTrue(owner.signupValid)
        assertFalse(owner.copy(fields = owner.fields - AuthField.STORE_NAME).signupValid)
    }
    @Test fun stateDiagnosticsDoNotContainCredentialsOrVerificationCodes() {
        val state = worker().copy(fields = worker().fields + (AuthField.EMAIL_CODE to "123456"))
        assertFalse(state.toString().contains("synthetic-password"))
        assertFalse(state.toString().contains("123456"))
        assertFalse(state.toString().contains("worker@example.com"))
    }
}
