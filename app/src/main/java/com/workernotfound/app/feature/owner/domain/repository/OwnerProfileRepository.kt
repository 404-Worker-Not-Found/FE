package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.NotificationSettings
import com.workernotfound.app.feature.owner.domain.model.OwnerProfile

/**
 * Owner profile for 마이페이지 (UI spec 4-1). Photo upload and notification preferences have no
 * confirmed backend contract yet. Account actions live in the account feature.
 */
interface OwnerProfileRepository {
    suspend fun getProfile(): OwnerProfile

    suspend fun updatePhoto(uri: String)

    suspend fun updateName(name: String)

    suspend fun updateNotificationSettings(settings: NotificationSettings)
}
