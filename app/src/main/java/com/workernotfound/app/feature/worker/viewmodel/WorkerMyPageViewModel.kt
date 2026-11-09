package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.auth.domain.AuthRepository
import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import com.workernotfound.app.feature.worker.domain.model.WorkerProfile
import com.workernotfound.app.feature.worker.domain.repository.WorkerProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkerMyPageUiState(
    val profile: WorkerProfile? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val nameDraft: String? = null,
    val isNameValid: Boolean = true,
    val isSaving: Boolean = false,
    val hasNameSaveError: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isWithdrawDialogVisible: Boolean = false,
)

enum class WorkerNotificationChannel { PUSH, EMAIL, SMS }

@HiltViewModel
class WorkerMyPageViewModel @Inject constructor(
    private val repository: WorkerProfileRepository,
    private val auth: AuthRepository,
    private val backend: BackendAccess,
) : ViewModel() {
    private val state = MutableStateFlow(WorkerMyPageUiState())
    val uiState = state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.profile.collect { profile -> state.update { it.copy(profile = profile) } }
        }
        load()
    }

    fun load() {
        state.update { it.copy(isLoading = true, profile = null, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.load()
                // A cached demo value may equal the existing StateFlow value and not emit again.
                state.update { it.copy(profile = repository.profile.value) }
            }
            catch (error: Exception) { report(error) }
            finally { state.update { it.copy(isLoading = false) } }
        }
    }

    fun updatePhoto(uri: String) {
        val profile = state.value.profile ?: return
        save(profile.copy(profileImageUri = uri))
    }
    fun editName() = state.update { it.copy(nameDraft = it.profile?.name, isNameValid = true, hasNameSaveError = false) }
    fun changeName(name: String) = state.update {
        it.copy(nameDraft = name, isNameValid = ProfileNamePolicy.isValid(name), hasNameSaveError = false)
    }
    fun dismissName() { if (!state.value.isSaving) state.update { it.copy(nameDraft = null) } }
    fun saveName() {
        val current = state.value
        val profile = current.profile ?: return
        val name = current.nameDraft ?: return
        if (!ProfileNamePolicy.isValid(name)) return
        save(profile.copy(name = name), editingName = true)
    }

    fun setNotification(channel: WorkerNotificationChannel, enabled: Boolean) {
        val profile = state.value.profile ?: return
        save(when (channel) {
            WorkerNotificationChannel.PUSH -> profile.copy(isPushEnabled = enabled)
            WorkerNotificationChannel.EMAIL -> profile.copy(isEmailEnabled = enabled)
            WorkerNotificationChannel.SMS -> profile.copy(isSmsEnabled = enabled)
        })
    }

    private fun save(profile: WorkerProfile, editingName: Boolean = false) {
        if (state.value.isSaving) return
        state.update { it.copy(isSaving = true, errorMessage = null, hasNameSaveError = false) }
        viewModelScope.launch {
            try {
                repository.updateProfile(profile)
                if (editingName) state.update { it.copy(nameDraft = null) }
            } catch (error: Exception) {
                report(error)
                if (editingName) state.update { it.copy(hasNameSaveError = true) }
            } finally { state.update { it.copy(isSaving = false) } }
        }
    }

    fun logout() {
        if (state.value.isSaving) return
        state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                if (!BuildConfig.USE_DEMO_DATA) auth.logout()
                state.update { it.copy(isLoggedOut = true) }
            } catch (error: Exception) { report(error) }
            finally { state.update { it.copy(isSaving = false) } }
        }
    }

    private fun report(error: Exception) {
        if (error is CancellationException) throw error
        state.update { it.copy(errorMessage = backend.message(error)) }
    }
    fun showWithdrawDialog() = state.update { it.copy(isWithdrawDialogVisible = true) }
    fun dismissWithdrawDialog() = state.update { it.copy(isWithdrawDialogVisible = false) }
}
