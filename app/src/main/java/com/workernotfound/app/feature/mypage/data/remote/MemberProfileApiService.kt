package com.workernotfound.app.feature.mypage.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface MemberProfileApiService {
    @GET("api/members/me")
    suspend fun get(): ApiResponse<MemberProfileDto>

    @PATCH("api/members/me")
    suspend fun updateName(@Body request: UpdateMemberNameDto): ApiResponse<MemberProfileDto>
}

data class UpdateMemberNameDto(val name: String)
data class MemberProfileDto(
    val memberId: Long,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val role: String,
    val ownerProfile: OwnerBusinessProfileDto?,
    val workerProfile: WorkerPreferenceDto? = null,
)
/** member-service WorkerProfileResponse: preferences entered at worker signup. */
data class WorkerPreferenceDto(
    val desiredHourlyWage: Int?,
    val activityRadiusKm: Int?,
    val baseLocation: WorkerBaseLocationDto?,
    val preferredBusinessTypes: List<String>?,
    val availableTimes: List<WorkerAvailableTimeDto>?,
)
data class WorkerBaseLocationDto(val address: String?, val latitude: Double?, val longitude: Double?)
data class WorkerAvailableTimeDto(val dayOfWeek: String?, val startTime: String?, val endTime: String?)
data class OwnerBusinessProfileDto(val storeName: String, val storeLocation: ProfileStoreLocationDto? = null)
data class ProfileStoreLocationDto(val address: String?)
