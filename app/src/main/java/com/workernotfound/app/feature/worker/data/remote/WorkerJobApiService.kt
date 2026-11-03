package com.workernotfound.app.feature.worker.data.remote

import com.workernotfound.app.core.network.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface WorkerJobApiService {
    @GET("api/jobs/search")
    suspend fun search(@QueryMap query: Map<String, String>): ApiResponse<JobSearchResponseDto>

    @GET("api/jobs/{id}")
    suspend fun detail(@Path("id") id: Long): ApiResponse<JobDetailResponseDto>
}

data class JobSearchResponseDto(val jobs: List<JobCardResponseDto>, val totalCount: Int, val totalPages: Int)
data class JobCardResponseDto(
    val id: Long, val storeName: String, val categoryName: String,
    val workDate: String, val startTime: String, val endTime: String,
    val baseHourlyWage: Int, val extraWage: Int, val remainingMinutes: Long,
    val distanceKm: Double?, @SerializedName("isUrgent") val isUrgent: Boolean, val status: String,
)
data class JobDetailResponseDto(
    val id: Long, val storeName: String, val categoryName: String, val address: String,
    val baseHourlyWage: Int, val extraWage: Int, val workDate: String,
    val startTime: String, val endTime: String, val workHours: Int, val workMinutes: Int,
    val description: String, val applicantCount: Int?, val recruitCount: Int,
)
