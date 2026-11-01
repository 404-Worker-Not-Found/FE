package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import com.workernotfound.app.feature.worker.domain.model.WorkerProfile
import com.workernotfound.app.feature.worker.domain.repository.WorkerProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkerMyPageUiState(
    val profile: WorkerProfile,
    val nameDraft: String? = null,
    val isNameValid: Boolean = true,
    val isWithdrawDialogVisible: Boolean = false,
)

enum class WorkerNotificationChannel { PUSH, EMAIL, SMS }

@HiltViewModel
class WorkerMyPageViewModel @Inject constructor(
    private val repository: WorkerProfileRepository,
) : ViewModel() {
    private val state = MutableStateFlow(WorkerMyPageUiState(repository.profile.value))
    val uiState = state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.profile.collect { profile -> state.update { it.copy(profile = profile) } }
        }
    }

    fun updatePhoto(uri: String) = repository.updateProfile(repository.profile.value.copy(profileImageUri = uri))

    fun editName() = state.update { it.copy(nameDraft = it.profile.name, isNameValid = true) }
    fun changeName(name: String) = state.update {
        it.copy(nameDraft = name, isNameValid = ProfileNamePolicy.isValid(name))
    }
    fun dismissName() = state.update { it.copy(nameDraft = null) }
    fun saveName() {
        val name = state.value.nameDraft ?: return
        if (!ProfileNamePolicy.isValid(name)) return
        repository.updateProfile(repository.profile.value.copy(name = name))
        dismissName()
    }

    fun setNotification(channel: WorkerNotificationChannel, enabled: Boolean) {
        val profile = repository.profile.value
        repository.updateProfile(when (channel) {
            WorkerNotificationChannel.PUSH -> profile.copy(isPushEnabled = enabled)
            WorkerNotificationChannel.EMAIL -> profile.copy(isEmailEnabled = enabled)
            WorkerNotificationChannel.SMS -> profile.copy(isSmsEnabled = enabled)
        })
    }

    fun showWithdrawDialog() = state.update { it.copy(isWithdrawDialogVisible = true) }
    fun dismissWithdrawDialog() = state.update { it.copy(isWithdrawDialogVisible = false) }
}
