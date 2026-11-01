package com.workernotfound.app.feature.worker.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WorkerWorkApiService {
    @GET("api/works/me")
    suspend fun list(@Query("page") page: Int, @Query("size") size: Int = 100): ApiResponse<WorkPageResponseDto>
}
data class WorkPageResponseDto(val content: List<WorkResponseDto>, val totalPages: Int)
data class WorkResponseDto(val workId: Long, val jobPostId: Long, val workDate: String, val startTime: String, val endTime: String, val endTimeNextDay: Boolean, val status: String)
