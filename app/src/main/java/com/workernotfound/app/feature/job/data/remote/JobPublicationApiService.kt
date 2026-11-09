package com.workernotfound.app.feature.job.data.remote

import com.workernotfound.app.core.network.ApiResponse
import java.math.BigDecimal
import retrofit2.http.*

interface JobPublicationApiService {
    @GET("api/jobs/categories") suspend fun categories(): ApiResponse<JobCategoryListDto>
    @GET("api/members/me") suspend fun me(): ApiResponse<PostingMemberDto>
    @POST("api/jobs") suspend fun create(@Body request: CreateJobDto): ApiResponse<Long>
    @GET("api/jobs/{id}/payment-order") suspend fun payment(@Path("id") id: Long): ApiResponse<JobPaymentDto>
    @GET("api/payments/orders/{orderId}") suspend fun order(@Path("orderId") id: String): ApiResponse<PaymentOrderDto>
}

data class PostingMemberDto(val role: String, val ownerProfile: PostingBusinessDto?)
data class PostingBusinessDto(val ownerProfileId: Long, val storeName: String, val storeLocation: PostingLocationDto)
data class PostingLocationDto(val address: String, val latitude: BigDecimal, val longitude: BigDecimal)
data class CreateJobDto(
    val businessId: Long, val categoryId: Long, val storeName: String, val address: String,
    val title: String, val description: String, val workDate: String, val startTime: String, val endTime: String,
    val isEndTimeNextDay: Boolean, val baseHourlyWage: Int, val extraWage: Int?, val recruitCount: Int,
    val latitude: BigDecimal, val longitude: BigDecimal, val urgencyLevel: String, val applicationDeadline: String,
)
data class JobPaymentDto(
    val jobPostId: Long, val jobStatus: String, val orderCreationStatus: String,
    val paymentOrderId: String?, val paymentJobVersion: Long?, val amount: BigDecimal?, val currency: String?,
    val latestChange: PaymentChangeDto?,
)
data class PaymentChangeDto(val status: String)
data class PaymentOrderDto(val orderId: String, val jobPostId: Long, val jobVersion: Long,
    val amount: BigDecimal, val currency: String, val status: String)

data class JobCategoryListDto(val categories: List<JobCategoryDto>)
data class JobCategoryDto(val id: Long, val name: String, val subCategories: List<JobSubCategoryDto>)
data class JobSubCategoryDto(val id: Long, val name: String)
