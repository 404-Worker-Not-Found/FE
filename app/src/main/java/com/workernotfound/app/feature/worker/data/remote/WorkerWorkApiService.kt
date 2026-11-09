package com.workernotfound.app.feature.worker.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkerWorkApiService {
    @POST("api/works/me/{id}/check-in")
    suspend fun checkIn(@Path("id") id: Long, @Body request: WorkCheckInRequestDto): ApiResponse<WorkResponseDto>
    @GET("api/works/me/{id}")
    suspend fun detail(@Path("id") id: Long): ApiResponse<WorkResponseDto>
    @GET("api/works/me/attendance-policy")
    suspend fun policy(): ApiResponse<AttendancePolicyResponseDto>
    @POST("api/works/me/{id}/start")
    suspend fun start(@Path("id") id: Long): ApiResponse<WorkResponseDto>
    @POST("api/works/me/{id}/complete")
    suspend fun complete(@Path("id") id: Long): ApiResponse<WorkResponseDto>

    @GET("api/works/me")
    suspend fun list(@Query("page") page: Int, @Query("size") size: Int = 100): ApiResponse<WorkPageResponseDto>
}
data class WorkPageResponseDto(val content: List<WorkResponseDto>, val totalPages: Int)
data class WorkResponseDto(val workId: Long, val jobPostId: Long, val workDate: String, val startTime: String, val endTime: String, val endTimeNextDay: Boolean, val status: String, val workerMemberId: Long, val checkedInAt: String?, val startedAt: String?, val latitude: Double? = null, val longitude: Double? = null, val confirmedAt: String? = null, val completedAt: String? = null)

data class AttendancePolicyResponseDto(val timeZone: String, val allowEarlyCompletion: Boolean, val completionRole: String, val radiusMeters: Double? = null, val earlyWindowSeconds: Long? = null, val lateWindowSeconds: Long? = null)
data class WorkCheckInRequestDto(val latitude: Double, val longitude: Double)
