package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.auth.domain.AuthRepository
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
    private val auth: AuthRepository,
    private val backend: BackendAccess,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerMyPageUiState())
    val uiState: StateFlow<OwnerMyPageUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, profile = null, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getProfile() }
                .onSuccess { profile -> _uiState.update { it.copy(isLoading = false, profile = profile) } }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(isLoading = false, errorMessage = backend.message(t)) }
                }
        }
    }

    fun setNotification(channel: NotificationChannel, isEnabled: Boolean) {
        val profile = _uiState.value.profile ?: return
        if (_uiState.value.isSavingNotification) return
        val previous = profile.notificationSettings ?: return
        val settings = previous.with(channel, isEnabled)
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

    fun logout() {
        if (_uiState.value.isLoggingOut) return
        _uiState.update { it.copy(isLoggingOut = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                auth.logout()
                _uiState.update { it.copy(isLoggedOut = true) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = backend.message(error)) }
            } finally { _uiState.update { it.copy(isLoggingOut = false) } }
        }
    }

    private fun NotificationSettings.with(channel: NotificationChannel, isEnabled: Boolean) = when (channel) {
        NotificationChannel.PUSH -> copy(isPushEnabled = isEnabled)
        NotificationChannel.EMAIL -> copy(isEmailEnabled = isEnabled)
        NotificationChannel.SMS -> copy(isSmsEnabled = isEnabled)
    }
}
