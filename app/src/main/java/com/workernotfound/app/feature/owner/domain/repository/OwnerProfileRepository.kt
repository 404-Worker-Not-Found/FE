package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.NotificationSettings
import com.workernotfound.app.feature.owner.domain.model.OwnerProfile

/**
 * Owner profile for 마이페이지 (UI spec 4-1). Mock-backed; account actions
 * (name/phone edit with re-verification, logout, withdrawal) need the auth
 * backend and are not implemented (decision: Demo Scope and Mock-First Strategy).
 */
interface OwnerProfileRepository {
    suspend fun getProfile(): OwnerProfile

    suspend fun updateNotificationSettings(settings: NotificationSettings)
}
