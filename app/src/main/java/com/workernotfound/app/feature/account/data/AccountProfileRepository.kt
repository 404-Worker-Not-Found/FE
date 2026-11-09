package com.workernotfound.app.feature.account.data

import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.mypage.data.remote.UpdateMemberNameDto
import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import javax.inject.Inject

import com.workernotfound.app.feature.account.domain.AccountProfile
class AccountProfileRepository @Inject constructor(private val api: MemberProfileApiService) {
    suspend fun load(): AccountProfile = api.get().requireData().let { AccountProfile(it.name, it.phoneNumber) }
    suspend fun rename(name: String): AccountProfile {
        require(ProfileNamePolicy.isValid(name))
        return api.updateName(UpdateMemberNameDto(name)).requireData().let { AccountProfile(it.name, it.phoneNumber) }
    }
}
