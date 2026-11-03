package com.workernotfound.app.feature.auth.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponse<LoginResponseDto>

    @POST("api/auth/signup/owner")
    suspend fun signupOwner(@Body request: OwnerSignupDto): ApiResponse<LoginResponseDto>
    @POST("api/auth/signup/worker")
    suspend fun signupWorker(@Body request: WorkerSignupDto): ApiResponse<LoginResponseDto>
    @POST("api/auth/email-verifications/send")
    suspend fun sendEmail(@Body request: EmailDto): ApiResponse<VerificationDto>
    @POST("api/auth/email-verifications/verify")
    suspend fun verifyEmail(@Body request: EmailCodeDto): ApiResponse<VerificationDto>
    @POST("api/auth/sms-verifications/send")
    suspend fun sendSms(@Body request: PhoneDto): ApiResponse<VerificationDto>
    @POST("api/auth/sms-verifications/verify")
    suspend fun verifySms(@Body request: PhoneCodeDto): ApiResponse<VerificationDto>
    @POST("api/auth/password-resets/email-verifications/send")
    suspend fun sendReset(@Body request: EmailDto): ApiResponse<VerificationDto>
    @POST("api/auth/password-resets")
    suspend fun resetPassword(@Body request: PasswordResetDto): ApiResponse<Unit>

    // Synchronous separate client: invoked by OkHttp's authenticator, never the UI thread.
    @POST("api/auth/tokens/reissue")
    fun reissue(@Body request: TokenReissueRequestDto): Call<ApiResponse<TokenResponseDto>>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): ApiResponse<Unit>
}

class LoginRequestDto(val email: String, val password: String, val deviceId: String)
class TokenReissueRequestDto(val refreshToken: String, val deviceId: String)
class LogoutRequestDto(val refreshToken: String)
class LoginResponseDto(val memberId: Long, val role: String, val tokenResponse: TokenResponseDto)
class TokenResponseDto(val accessToken: String, val refreshToken: String)
