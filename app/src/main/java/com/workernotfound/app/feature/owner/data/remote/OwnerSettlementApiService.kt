package com.workernotfound.app.feature.owner.data.remote

import com.workernotfound.app.core.network.ApiResponse
import com.workernotfound.app.feature.owner.domain.model.ServerSettlement
import java.math.BigDecimal
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OwnerSettlementApiService {
    @GET("api/payments/me/settlements")
    suspend fun list(@Query("beforeId") beforeId: Long?, @Query("size") size: Int = 100): ApiResponse<List<SettlementResponseDto>>

    @GET("api/payments/me/settlements/{id}")
    suspend fun detail(@Path("id") id: Long): ApiResponse<SettlementResponseDto>
}

data class SettlementResponseDto(
    val id: Long,
    val workId: Long,
    val jobPostId: Long,
    val amount: BigDecimal,
    val currency: String,
    val status: String,
    val holdReason: String?,
    val payoutReviewRequired: Boolean,
    val workCompletedAt: String?,
    val payableAt: String?,
    val paidAt: String?,
) {
    fun toDomain() = ServerSettlement(id, workId, jobPostId, amount, currency, status, holdReason,
        payoutReviewRequired, workCompletedAt, payableAt, paidAt)
}
