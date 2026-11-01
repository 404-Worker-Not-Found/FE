package com.workernotfound.app.feature.owner.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OwnerApplicantApiService {
    @GET("api/jobs/{jobId}/applications")
    suspend fun list(@Path("jobId") jobId: Long, @Query("page") page: Int, @Query("scoreBatchId") scoreBatchId: Long?, @Query("size") size: Int = 100): ApiResponse<OwnerApplicantListResponseDto>
    @GET("api/jobs/{jobId}/applications/{applicationId}")
    suspend fun detail(@Path("jobId") jobId: Long, @Path("applicationId") applicationId: Long): ApiResponse<OwnerApplicantResponseDto>
    @POST("api/jobs/{jobId}/applications/{applicationId}/matchings")
    suspend fun propose(@Path("jobId") jobId: Long, @Path("applicationId") applicationId: Long, @Body request: CreateManualMatchingRequestDto = CreateManualMatchingRequestDto()): ApiResponse<ManualMatchingResponseDto>
}
data class OwnerApplicantListResponseDto(val scoreBatchId: Long?, val totalPages: Int, val applicants: List<OwnerApplicantResponseDto>)
data class OwnerApplicantResponseDto(val applicationId: Long, val workerMemberId: Long, val workerName: String?, val status: String, val totalScore: Double?, val expectedArrivalMinutes: Int?, val noShowProbability: Double?)
data class CreateManualMatchingRequestDto(val scoreBatchId: Long? = null)
data class ManualMatchingResponseDto(val matchingId: Long, val status: String)
