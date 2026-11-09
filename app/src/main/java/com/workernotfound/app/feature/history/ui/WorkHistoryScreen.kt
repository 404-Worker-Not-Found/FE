package com.workernotfound.app.feature.history.ui

import com.workernotfound.app.core.ui.KoreanDateTime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.history.domain.*
import com.workernotfound.app.feature.owner.ui.component.ServerSettlementCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class WorkHistoryUiState(val loading: Boolean = true, val entries: List<WorkHistoryEntry> = emptyList(), val error: String? = null)
@HiltViewModel
class WorkHistoryViewModel @Inject constructor(private val repository: WorkHistoryRepository,
    private val backend: BackendAccess) : ViewModel() {
    private val state = MutableStateFlow(WorkHistoryUiState())
    val uiState = state.asStateFlow()
    private var request: Job? = null
    fun load() {
        if (request?.isActive == true) return
        request = viewModelScope.launch {
            state.update { it.copy(loading = true, error = null) }
            try { state.value = WorkHistoryUiState(loading = false, entries = repository.getAll()) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state.update { it.copy(loading = false, error = backend.message(e)) } }
        }
    }
}
@Composable
fun WorkHistoryScreen(onBack: () -> Unit, onJob: (String) -> Unit, onReviews: () -> Unit,
    modifier: Modifier = Modifier, viewModel: WorkHistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    Column(modifier.fillMaxSize().background(AppColors.WorkerSurface).statusBarsPadding()) {
        AppTopBar(title = stringResource(R.string.history_title), onBack = onBack)
        TextButton(onReviews) { Text(stringResource(R.string.reviews_title)) }
        when {
            state.loading -> CircularProgressIndicator(modifier = Modifier.padding(AppDimens.screenPadding))
            state.error != null -> Column(Modifier.padding(AppDimens.screenPadding)) {
                Text(requireNotNull(state.error), color = AppColors.Danger)
                TextButton(viewModel::load) { Text(stringResource(R.string.onboarding_retry)) }
            }
            else -> LazyColumn(contentPadding = PaddingValues(AppDimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
                if (state.entries.isEmpty()) item { Text(stringResource(R.string.history_empty), color = AppColors.TextSub) }
                items(state.entries, key = { it.workId }) { entry ->
                    AppCard {
                        Text(entry.storeName, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.history_work_ref, entry.workId), color = AppColors.TextSub)
                        Text("${entry.date} · ${entry.startTime.take(5)} ~ ${entry.endTime.take(5)}")
                        if (entry.nextDay) Text(stringResource(R.string.history_next_day), color = AppColors.TextSub)
                        Text(stringResource(when (entry.status) {
                            "COMPLETED" -> R.string.history_completed
                            "CANCELED" -> R.string.history_canceled
                            "NO_SHOW" -> R.string.history_no_show
                            else -> R.string.history_failed
                        }))
                        entry.completedAt?.let { Text(stringResource(R.string.history_completed_at, KoreanDateTime.dateTime(it)), color = AppColors.TextSub) }
                        TextButton({ onJob(entry.jobId.toString()) }) { Text(stringResource(R.string.history_open_job)) }
                        ServerSettlementCard(entry.settlement)
                    }
                }
            }
        }
    }
}
