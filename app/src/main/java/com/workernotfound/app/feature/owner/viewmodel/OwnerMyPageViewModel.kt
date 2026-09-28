package com.workernotfound.app.feature.owner.viewmodel

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
                    _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "프로필을 불러오지 못했습니다.") }
                }
        }
    }

    fun setNotification(channel: NotificationChannel, isEnabled: Boolean) {
        val profile = _uiState.value.profile ?: return
        val settings = profile.notificationSettings.with(channel, isEnabled)
        _uiState.update { it.copy(profile = profile.copy(notificationSettings = settings)) }
        viewModelScope.launch { repository.updateNotificationSettings(settings) }
    }

    fun showWithdrawDialog() = _uiState.update { it.copy(isWithdrawDialogVisible = true) }

    fun dismissWithdrawDialog() = _uiState.update { it.copy(isWithdrawDialogVisible = false) }

    private fun NotificationSettings.with(channel: NotificationChannel, isEnabled: Boolean) = when (channel) {
        NotificationChannel.PUSH -> copy(isPushEnabled = isEnabled)
        NotificationChannel.EMAIL -> copy(isEmailEnabled = isEnabled)
        NotificationChannel.SMS -> copy(isSmsEnabled = isEnabled)
    }
}
