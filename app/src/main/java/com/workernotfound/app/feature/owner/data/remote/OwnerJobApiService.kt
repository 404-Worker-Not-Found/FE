package com.workernotfound.app.feature.owner.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OwnerJobApiService {
    @GET("api/jobs/me")
    suspend fun list(@Query("page") page: Int, @Query("size") size: Int = 100): ApiResponse<OwnerJobListResponseDto>
}
data class OwnerJobListResponseDto(val jobs: List<OwnerJobCardResponseDto>, val totalPages: Int)
data class OwnerJobCardResponseDto(val id: Long, val title: String, val status: String, val isFundingBlocked: Boolean, val workDate: String, val startTime: String, val endTime: String, val isEndTimeNextDay: Boolean, val applicantCount: Int?)
