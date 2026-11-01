package com.workernotfound.app.feature.worker.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkerApplicationApiService {
    @GET("api/applications")
    suspend fun list(@Query("page") page: Int, @Query("size") size: Int = 100): ApiResponse<ApplicationListResponseDto>
    @GET("api/applications/{id}")
    suspend fun detail(@Path("id") id: Long): ApiResponse<ApplicationResponseDto>
    @PATCH("api/applications/{id}/cancel")
    suspend fun cancel(@Path("id") id: Long): ApiResponse<ApplicationResponseDto>

    @POST("api/applications")
    suspend fun apply(@Body request: CreateApplicationRequestDto): ApiResponse<ApplicationResponseDto>
}

data class CreateApplicationRequestDto(val jobPostId: Long)
data class ApplicationResponseDto(val applicationId: Long, val jobPostId: Long, val status: String, val appliedAt: String)

data class ApplicationListResponseDto(val applications: List<ApplicationResponseDto>, val totalPages: Int)
