package com.workernotfound.app.feature.account.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.account.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AccountUiState(val channel: ContactChannel = ContactChannel.PHONE, val target: String = "",
    val code: String = "", val busy: Boolean = false, val cooldown: Int = 0,
    val profile: com.workernotfound.app.feature.account.domain.AccountProfile? = null,
    val nameDraft: String? = null, val nameSaving: Boolean = false, val nameError: Boolean = false,
    val invalidCode: Boolean = false,
    val sent: Boolean = false, val key: String? = null, val withdrawing: Boolean = false,
    val command: AccountCommand? = null, val confirmWithdrawal: Boolean = false, val error: String? = null) {
    val validTarget get() = if (channel == ContactChannel.EMAIL) target.length <= 255 && Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+").matches(target)
        else Regex("\\+?[0-9]{10,15}").matches(target)
    override fun toString() = "AccountUiState(busy=$busy, withdrawing=$withdrawing)"
}
@HiltViewModel
class AccountViewModel @Inject constructor(private val repository: AccountRepository,
    private val profiles: com.workernotfound.app.feature.account.data.AccountProfileRepository,
    private val backend: BackendAccess, private val saved: SavedStateHandle) : ViewModel() {
    private val state = MutableStateFlow(AccountUiState(key = saved["accountCommand"], withdrawing = saved["withdrawing"] ?: false))
    val uiState = state.asStateFlow()
    init { viewModelScope.launch { while (true) { delay(1000); state.update { it.copy(cooldown = (it.cooldown - 1).coerceAtLeast(0)) } } } }
    init { action { state.update { it.copy(profile = profiles.load()) } } }
    // 마이페이지 '회원 탈퇴' opens this screen with the existing confirmation dialog already shown (once).
    init {
        if (saved.get<Boolean>("withdraw") == true && saved.get<Boolean>("withdrawPrompted") != true) {
            saved["withdrawPrompted"] = true
            // Not via confirmWithdrawal(): the profile load above marks the screen busy right now.
            // The dialog only asks; withdraw() still refuses while busy or with a pending command.
            if (state.value.key == null) state.update { it.copy(confirmWithdrawal = true) }
        }
    }
    fun editName() { state.update { it.copy(nameDraft = it.profile?.name, nameError = false) } }
    fun name(value: String) { state.update { it.copy(nameDraft = value, nameError = false) } }
    fun dismissName() { if (!state.value.nameSaving) state.update { it.copy(nameDraft = null) } }
    fun saveName() {
        val name = state.value.nameDraft ?: return
        if (!com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy.isValid(name) || state.value.nameSaving) return
        state.update { it.copy(nameSaving = true) }
        viewModelScope.launch {
            try { val profile = profiles.rename(name); state.update { it.copy(profile = profile, nameDraft = null) } }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state.update { it.copy(nameError = true) } }
            finally { state.update { it.copy(nameSaving = false) } }
        }
    }
    fun channel(value: ContactChannel) { if (!state.value.busy && state.value.key == null) state.update { it.copy(channel = value, target = "", code = "", sent = false, error = null, invalidCode = false) } }
    fun target(value: String) { if (!state.value.busy && state.value.key == null) state.update { it.copy(target = value, code = "", sent = false, error = null, invalidCode = false) } }
    fun code(value: String) { if (!state.value.busy && state.value.key == null) state.update { it.copy(code = value, invalidCode = false) } }
    fun send() {
        val s = state.value
        if (!s.validTarget || s.cooldown > 0 || s.key != null) return
        action { repository.send(s.channel, s.target); state.update { it.copy(sent = true, cooldown = 60) } }
    }
    fun change() {
        val s = state.value
        if (s.busy || !s.validTarget || !s.sent || !Regex("[0-9]{6}").matches(s.code) || s.withdrawing) return
        val key = key(false)
        action { accept(repository.change(key, s.channel, s.target, s.code)) }
    }
    fun confirmWithdrawal(show: Boolean) { if (!state.value.busy && state.value.key == null) state.update { it.copy(confirmWithdrawal = show) } }
    fun withdraw() {
        if (!state.value.confirmWithdrawal || state.value.busy) return
        val key = key(true)
        state.update { it.copy(confirmWithdrawal = false) }
        action { accept(repository.withdraw(key)) }
    }
    fun refresh() {
        val s = state.value
        val key = s.key ?: return
        action { accept(if (s.withdrawing) repository.withdrawalStatus(key) else repository.contactStatus(key)) }
    }
    private fun key(withdrawing: Boolean): String {
        val key = state.value.key ?: UUID.randomUUID().toString()
        saved["accountCommand"] = key; saved["withdrawing"] = withdrawing
        state.update { it.copy(key = key, withdrawing = withdrawing) }
        return key
    }
    private suspend fun accept(command: AccountCommand) {
        if (command.terminal) repository.clearCompletedSession()
        state.update { it.copy(command = command, target = "", code = "") }
    }
    private fun action(block: suspend () -> Unit) {
        if (state.value.busy) return
        state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (_: ContactCodeRejected) {
                saved.remove<String>("accountCommand"); saved.remove<Boolean>("withdrawing")
                state.update { it.copy(key = null, command = null, withdrawing = false,
                    code = "", invalidCode = true, error = null) }
            }
            catch (e: Exception) { state.update { it.copy(error = backend.message(e)) } }
            finally { state.update { it.copy(busy = false) } }
        }
    }
}
