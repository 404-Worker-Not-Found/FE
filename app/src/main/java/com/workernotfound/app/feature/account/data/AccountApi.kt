package com.workernotfound.app.feature.account.data

import com.workernotfound.app.core.network.ApiResponse
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.account.domain.AccountCommand
import retrofit2.http.*

interface AccountApi {
    @POST("api/auth/account/email-verifications/send") suspend fun email(@Body body: EmailTarget): ApiResponse<Unit>
    @POST("api/auth/account/sms-verifications/send") suspend fun phone(@Body body: PhoneTarget): ApiResponse<Unit>
    @POST("api/auth/account/contact-changes") suspend fun change(@Header("Idempotency-Key") key: String, @Body body: ContactChangeBody): ApiResponse<ContactCommandDto>
    @GET("api/auth/account/contact-changes/{id}") suspend fun contact(@Path("id") key: String): ApiResponse<ContactCommandDto>
    @POST("api/auth/account/withdrawals") suspend fun withdraw(@Header("Idempotency-Key") key: String): ApiResponse<WithdrawalDto>
    @GET("api/auth/account/withdrawals/{id}") suspend fun withdrawal(@Path("id") key: String): ApiResponse<WithdrawalDto>
}
class EmailTarget(val email: String)
class PhoneTarget(val phoneNumber: String)
class ContactChangeBody(val channel: String, val target: String, val verificationCode: String)
data class ContactCommandDto(val commandId: String, val status: String) {
    fun toDomain(key: String) = confirmedCommand(key, commandId, status)
}
data class WithdrawalDto(val commandId: String, val state: String) {
    fun toDomain(key: String) = confirmedCommand(key, commandId, state)
}
private fun confirmedCommand(key: String, id: String, state: String): AccountCommand {
    if (key != id || state.isBlank()) throw ApiException("ACCOUNT_COMMAND_MISMATCH")
    return AccountCommand(id, state)
}
