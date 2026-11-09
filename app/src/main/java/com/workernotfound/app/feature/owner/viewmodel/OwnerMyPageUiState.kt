package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.OwnerProfile

/** 점주 마이페이지 상태 (UI spec 4-1). */
data class OwnerMyPageUiState(
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val profile: OwnerProfile? = null,
    val errorMessage: String? = null,
    val nameDraft: String? = null,
    val isNameValid: Boolean = true,
    val isSavingName: Boolean = false,
    val hasNameSaveError: Boolean = false,
    val hasPhotoSaveError: Boolean = false,
    val isSavingNotification: Boolean = false,
    val hasNotificationSaveError: Boolean = false,
)

/** Which notification channel a toggle controls (UI spec 4-1 알림 설정). */
enum class NotificationChannel { PUSH, EMAIL, SMS }
