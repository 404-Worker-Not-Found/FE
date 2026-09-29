package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.NotificationSettings
import com.workernotfound.app.feature.owner.domain.model.OwnerProfile
import com.workernotfound.app.feature.owner.domain.repository.OwnerProfileRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory owner profile; notification toggles persist for the app session only. */
@Singleton
class OwnerProfileRepositoryImpl @Inject constructor() : OwnerProfileRepository {

    private var profile = OwnerProfile(
        name = "김사장",
        storeName = "역삼 한식당 소반",
        email = "owner@example.com",
        phone = "010-9876-5432",
        profileImageUrl = null,
        notificationSettings = NotificationSettings(isPushEnabled = true, isEmailEnabled = false, isSmsEnabled = true),
    )

    override suspend fun getProfile(): OwnerProfile {
        delay(MOCK_DELAY_MILLIS)
        return profile
    }

    override suspend fun updateNotificationSettings(settings: NotificationSettings) {
        profile = profile.copy(notificationSettings = settings)
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
    }
}
