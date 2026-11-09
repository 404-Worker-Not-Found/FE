package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.mypage.data.remote.UpdateMemberNameDto
import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import com.workernotfound.app.feature.owner.domain.model.NotificationSettings
import com.workernotfound.app.feature.owner.domain.model.OwnerProfile
import com.workernotfound.app.feature.owner.domain.repository.OwnerProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerProfileRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val api: MemberProfileApiService,
) : OwnerProfileRepository {

    override suspend fun getProfile(): OwnerProfile {
        backend.requireSession()
        val member = api.get().requireData()
        if (member.role != "OWNER" || member.ownerProfile == null) throw ApiException("ROLE_MISMATCH")
        return OwnerProfile(member.name, member.ownerProfile.storeName, member.email, member.phoneNumber, null, null, member.ownerProfile.storeLocation?.address)
    }

    override suspend fun updateName(name: String) {
        require(ProfileNamePolicy.isValid(name))
        backend.requireSession()
        val member = api.updateName(UpdateMemberNameDto(name)).requireData()
        if (member.role != "OWNER") throw ApiException("ROLE_MISMATCH")
    }

    override suspend fun updatePhoto(uri: String) = backend.unsupported()

    override suspend fun updateNotificationSettings(settings: NotificationSettings) = backend.unsupported()
}
