package com.workernotfound.app.feature.auth.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponse<LoginResponseDto>

    // Synchronous separate client: invoked by OkHttp's authenticator, never the UI thread.
    @POST("api/auth/tokens/reissue")
    fun reissue(@Body request: TokenReissueRequestDto): Call<ApiResponse<TokenResponseDto>>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): ApiResponse<Unit>
}

data class LoginRequestDto(val email: String, val password: String, val deviceId: String)
data class TokenReissueRequestDto(val refreshToken: String, val deviceId: String)
data class LogoutRequestDto(val refreshToken: String)
data class LoginResponseDto(val memberId: Long, val role: String, val tokenResponse: TokenResponseDto)
data class TokenResponseDto(val accessToken: String, val refreshToken: String)
