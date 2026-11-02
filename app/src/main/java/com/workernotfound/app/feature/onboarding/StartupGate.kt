package com.workernotfound.app.feature.onboarding

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.datastore.SessionStore
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class StartupStage { SPLASH, ONBOARDING, READY, ERROR }
data class StartupUiState(val stage: StartupStage = StartupStage.SPLASH, val page: Int = 0, val saving: Boolean = false)

/** Non-sensitive first-run preference; authentication still belongs to AuthRepository. */
class OnboardingPreferences @Inject constructor(@ApplicationContext context: Context, private val sessions: SessionStore) {
    private val preferences = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
    suspend fun shouldShow(): Boolean = withContext(Dispatchers.IO) {
        !preferences.getBoolean("completed", false) && sessions.read() == null
    }
    suspend fun complete() = withContext(Dispatchers.IO) {
        check(preferences.edit().putBoolean("completed", true).commit())
    }
}
@HiltViewModel
class StartupViewModel @Inject constructor(private val preferences: OnboardingPreferences) : ViewModel() {
    private val state = MutableStateFlow(StartupUiState())
    val uiState = state.asStateFlow()
    init { load() }
    fun load() {
        if (state.value.saving) return
        viewModelScope.launch {
            state.value = StartupUiState()
            try {
                val start = System.currentTimeMillis()
                val show = preferences.shouldShow()
                if (!show) preferences.complete()
                delay((2000 - (System.currentTimeMillis() - start)).coerceAtLeast(0))
                state.value = StartupUiState(if (show) StartupStage.ONBOARDING else StartupStage.READY)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { state.value = StartupUiState(StartupStage.ERROR) }
        }
    }
    fun page(value: Int) { if (!state.value.saving) state.update { it.copy(page = value.coerceIn(0, 2)) } }
    fun finish() {
        if (state.value.saving) return
        state.update { it.copy(saving = true) }
        viewModelScope.launch {
            try { preferences.complete(); state.value = StartupUiState(StartupStage.READY) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { state.update { it.copy(saving = false, stage = StartupStage.ERROR) } }
        }
    }
}
@Composable
fun StartupGate(modifier: Modifier = Modifier, viewModel: StartupViewModel = hiltViewModel(), content: @Composable () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.stage == StartupStage.READY) { content(); return }
    Column(modifier.fillMaxSize().background(AppColors.WorkerSurface).safeDrawingPadding().padding(AppDimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null,
            modifier = Modifier.size(AppDimens.locationMapHeight))
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.onboarding_slogan), color = AppColors.TextSub)
        Spacer(Modifier.height(AppDimens.cardGap))
        when (state.stage) {
            StartupStage.SPLASH -> CircularProgressIndicator()
            StartupStage.ERROR -> {
                Text(stringResource(R.string.onboarding_failed), color = AppColors.Danger)
                AppPrimaryButton(stringResource(R.string.onboarding_retry), viewModel::load)
            }
            StartupStage.ONBOARDING -> {
                val titles = listOf(R.string.onboarding_find, R.string.onboarding_work, R.string.onboarding_record)
                val bodies = listOf(R.string.onboarding_find_body, R.string.onboarding_work_body, R.string.onboarding_record_body)
                Text(stringResource(titles[state.page]), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(bodies[state.page]), color = AppColors.TextSub)
                Text(stringResource(R.string.onboarding_page, state.page + 1), color = AppColors.TextSub)
                Row {
                    if (state.page > 0) TextButton({ viewModel.page(state.page - 1) }, enabled = !state.saving) { Text(stringResource(R.string.onboarding_previous)) }
                    if (state.page < 2) TextButton({ viewModel.page(state.page + 1) }, enabled = !state.saving) { Text(stringResource(R.string.onboarding_next)) }
                }
                AppPrimaryButton(stringResource(R.string.onboarding_start), viewModel::finish, enabled = !state.saving)
            }
            StartupStage.READY -> Unit
        }
    }
}
