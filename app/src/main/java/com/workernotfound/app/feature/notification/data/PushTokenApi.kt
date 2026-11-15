package com.workernotfound.app.feature.notification.data

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

interface PushTokenApi {
    @PUT("api/notifications/me/device-tokens")
    suspend fun register(@Body request: PushTokenRequestDto): ApiResponse<PushTokenResponseDto>

    @DELETE("api/notifications/me/device-tokens/{id}")
    suspend fun remove(@Path("id") id: Long): ApiResponse<Unit>
}

data class PushTokenRequestDto(val token: String, val platform: String = "ANDROID")
data class PushTokenResponseDto(val deviceTokenId: Long)
