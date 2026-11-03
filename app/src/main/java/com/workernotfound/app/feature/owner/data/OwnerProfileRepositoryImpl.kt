package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.mypage.data.remote.UpdateMemberNameDto
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.domain.model.NotificationSettings
import com.workernotfound.app.feature.owner.domain.model.OwnerProfile
import com.workernotfound.app.feature.owner.domain.repository.OwnerProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

/** In-memory owner profile; notification toggles persist for the app session only. */
@Singleton
class OwnerProfileRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val api: MemberProfileApiService,
) : OwnerProfileRepository {

    private var profile = OwnerProfile(
        name = "김사장",
        storeName = "역삼 한식당 소반",
        email = "owner@example.com",
        phone = "010-9876-5432",
        profileImageUrl = null,
        notificationSettings = NotificationSettings(isPushEnabled = true, isEmailEnabled = false, isSmsEnabled = true),
    )

    override suspend fun getProfile(): OwnerProfile {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val member = api.get().requireData()
            if (member.role != "OWNER" || member.ownerProfile == null) throw ApiException("ROLE_MISMATCH")
            return OwnerProfile(member.name, member.ownerProfile.storeName, member.email, member.phoneNumber, null, null)
        }
        delay(MOCK_DELAY_MILLIS)
        return profile
    }

    override suspend fun updatePhoto(uri: String) {
        backend.requireDemo()
        profile = profile.copy(profileImageUrl = uri)
    }

    override suspend fun updateName(name: String) {
        require(com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy.isValid(name))
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val member = api.updateName(UpdateMemberNameDto(name)).requireData()
            if (member.role != "OWNER") throw ApiException("ROLE_MISMATCH")
            return
        }
        profile = profile.copy(name = name)
    }

    override suspend fun updateNotificationSettings(settings: NotificationSettings) {
        backend.requireDemo()
        profile = profile.copy(notificationSettings = settings)
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
    }
}
