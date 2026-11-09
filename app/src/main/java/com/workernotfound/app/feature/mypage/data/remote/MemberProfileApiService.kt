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
)
data class OwnerBusinessProfileDto(val storeName: String, val storeLocation: ProfileStoreLocationDto? = null)
data class ProfileStoreLocationDto(val address: String?)
