package com.workernotfound.app.feature.cancellation.data

import com.workernotfound.app.core.network.*
import com.workernotfound.app.feature.cancellation.domain.Cancellation
import retrofit2.http.*

interface CancellationApi {
    @GET("api/works/me/{workId}/cancellations") suspend fun list(@Path("workId") work: Long): ApiResponse<List<CancellationDto>>
    @POST("api/works/me/{workId}/cancellations") suspend fun request(@Path("workId") work: Long, @Header("Idempotency-Key") key: String, @Body body: CancellationBody): ApiResponse<CancellationDto>
    @POST("api/works/me/{workId}/cancellations/{id}/approve") suspend fun approve(@Path("workId") work: Long, @Path("id") id: Long): ApiResponse<CancellationDto>
    @POST("api/works/me/{workId}/cancellations/{id}/reject") suspend fun reject(@Path("workId") work: Long, @Path("id") id: Long): ApiResponse<CancellationDto>
    @POST("api/works/me/{workId}/cancellations/{id}/withdraw") suspend fun withdraw(@Path("workId") work: Long, @Path("id") id: Long): ApiResponse<CancellationDto>
}
class CancellationBody(val reason: String)
data class CancellationDto(val requestId: Long, val workId: Long, val requesterMemberId: Long,
    val reason: String, val status: String, val createdAt: String) {
    fun toDomain(work: Long, id: Long? = null, expected: String? = null): Cancellation {
        if (workId != work || requestId <= 0 || (id != null && id != requestId) ||
            (expected != null && expected != status)) throw ApiException("CANCELLATION_NOT_CONFIRMED")
        return Cancellation(requestId, workId, requesterMemberId, reason, status, createdAt)
    }
}
