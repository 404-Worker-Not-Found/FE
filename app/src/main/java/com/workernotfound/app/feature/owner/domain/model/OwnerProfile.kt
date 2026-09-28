package com.workernotfound.app.feature.owner.domain.model

/** Owner account shown on 마이페이지 (UI spec 4-1 프로필 설정, owner fields). */
data class OwnerProfile(
    val name: String,
    val storeName: String,
    val email: String,
    val phone: String,
    val profileImageUrl: String?,
    val notificationSettings: NotificationSettings,
)

/** UI spec 4-1 알림 설정: Push / 이메일 / SMS ON/OFF. */
data class NotificationSettings(
    val isPushEnabled: Boolean,
    val isEmailEnabled: Boolean,
    val isSmsEnabled: Boolean,
)
