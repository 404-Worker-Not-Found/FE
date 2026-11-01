package com.workernotfound.app.feature.owner.viewmodel

import kotlinx.coroutines.CancellationException
import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.owner.domain.model.NotificationSettings
import com.workernotfound.app.feature.owner.domain.repository.OwnerProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerMyPageViewModel @Inject constructor(
    private val repository: OwnerProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerMyPageUiState())
    val uiState: StateFlow<OwnerMyPageUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getProfile() }
                .onSuccess { profile -> _uiState.update { it.copy(isLoading = false, profile = profile) } }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "프로필을 불러오지 못했습니다.") }
                }
        }
    }

    fun setNotification(channel: NotificationChannel, isEnabled: Boolean) {
        val profile = _uiState.value.profile ?: return
        if (_uiState.value.isSavingNotification) return
        val settings = profile.notificationSettings.with(channel, isEnabled)
        _uiState.update {
            it.copy(profile = profile.copy(notificationSettings = settings), isSavingNotification = true, hasNotificationSaveError = false)
        }
        viewModelScope.launch {
            runCatching { repository.updateNotificationSettings(settings) }
                .onSuccess { _uiState.update { it.copy(isSavingNotification = false) } }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update {
                        it.copy(
                            profile = it.profile?.copy(notificationSettings = profile.notificationSettings),
                            isSavingNotification = false, hasNotificationSaveError = true,
                        )
                    }
                }
        }
    }

    fun updatePhoto(uri: String) {
        _uiState.update { it.copy(hasPhotoSaveError = false) }
        viewModelScope.launch {
            runCatching { repository.updatePhoto(uri) }
                .onSuccess { _uiState.update { it.copy(profile = it.profile?.copy(profileImageUrl = uri)) } }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(hasPhotoSaveError = true) }
                }
        }
    }

    fun editName() = _uiState.update {
        it.copy(nameDraft = it.profile?.name, isNameValid = true, hasNameSaveError = false)
    }

    fun changeName(name: String) = _uiState.update {
        it.copy(nameDraft = name, isNameValid = ProfileNamePolicy.isValid(name), hasNameSaveError = false)
    }

    fun dismissName() {
        if (!_uiState.value.isSavingName) _uiState.update { it.copy(nameDraft = null) }
    }

    fun saveName() {
        val name = _uiState.value.nameDraft ?: return
        if (!ProfileNamePolicy.isValid(name) || _uiState.value.isSavingName) return
        _uiState.update { it.copy(isSavingName = true, hasNameSaveError = false) }
        viewModelScope.launch {
            runCatching { repository.updateName(name) }
                .onSuccess {
                    _uiState.update { it.copy(profile = it.profile?.copy(name = name), nameDraft = null, isSavingName = false) }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(isSavingName = false, hasNameSaveError = true) }
                }
        }
    }

    fun showWithdrawDialog() = _uiState.update { it.copy(isWithdrawDialogVisible = true) }

    fun dismissWithdrawDialog() = _uiState.update { it.copy(isWithdrawDialogVisible = false) }

    private fun NotificationSettings.with(channel: NotificationChannel, isEnabled: Boolean) = when (channel) {
        NotificationChannel.PUSH -> copy(isPushEnabled = isEnabled)
        NotificationChannel.EMAIL -> copy(isEmailEnabled = isEnabled)
        NotificationChannel.SMS -> copy(isSmsEnabled = isEnabled)
    }
}
