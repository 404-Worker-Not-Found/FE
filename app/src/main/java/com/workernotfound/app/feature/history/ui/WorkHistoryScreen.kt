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
import com.workernotfound.app.feature.owner.ui.component.formatSettlementAmount
import com.workernotfound.app.feature.owner.ui.component.settlementStatusResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        AppTopBar(
            title = stringResource(R.string.history_title),
            onBack = onBack,
            actions = { com.workernotfound.app.core.designsystem.component.AppSmallButton(stringResource(R.string.reviews_title), onReviews, modifier = Modifier.padding(end = 8.dp)) },
        )
        when {
            state.loading && state.entries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.role.accent)
            }
            state.error != null -> Column(
                Modifier.fillMaxSize().padding(AppDimens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(requireNotNull(state.error), color = AppColors.Danger)
                com.workernotfound.app.core.designsystem.component.AppSecondaryButton(stringResource(R.string.onboarding_retry), viewModel::load)
            }
            state.entries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), color = AppColors.TextSub)
            }
            else -> LazyColumn(contentPadding = PaddingValues(AppDimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
                items(state.entries, key = { it.workId }) { entry ->
                    HistoryCard(entry, onJob = { onJob(entry.jobId.toString()) })
                }
            }
        }
    }
}

/** 가게명·상태 → 날짜·시간 → 정산 요약 → 공고 보기. 내부 근무 ID는 노출하지 않는다. */
@Composable
private fun HistoryCard(entry: WorkHistoryEntry, onJob: () -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.storeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain, modifier = Modifier.weight(1f))
            StatusChip(entry.status)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${KoreanDateTime.workDate(entry.date)} · ${entry.startTime.take(5)} ~ ${entry.endTime.take(5)}",
            fontSize = 13.sp, color = AppColors.TextSub,
        )
        if (entry.nextDay) Text(stringResource(R.string.history_next_day), fontSize = 12.sp, color = AppColors.TextPlaceholder)
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = AppColors.Border)
        Spacer(Modifier.height(12.dp))
        val settlement = entry.settlement
        if (settlement == null) {
            Text(stringResource(R.string.settlement_preparing), fontSize = 13.sp, color = AppColors.TextSub)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.settlement_server_amount), fontSize = 13.sp, color = AppColors.TextSub, modifier = Modifier.weight(1f))
                Text(formatSettlementAmount(settlement.amount, settlement.currency), fontSize = 17.sp,
                    fontWeight = FontWeight.Bold, color = AppTheme.role.accentPressed)
            }
            Spacer(Modifier.height(6.dp))
            HistoryInfoLine(stringResource(R.string.settlement_server_status), stringResource(settlementStatusResource(settlement.status)))
            Spacer(Modifier.height(4.dp))
            HistoryInfoLine(
                stringResource(if (settlement.paidAt != null) R.string.settlement_paid_at else R.string.settlement_payable_at),
                (settlement.paidAt ?: settlement.payableAt)?.let(KoreanDateTime::dateTime) ?: stringResource(R.string.settlement_not_paid_yet),
            )
            if (settlement.payoutReviewRequired) Text(stringResource(R.string.settlement_review_required), fontSize = 12.sp, color = AppColors.Warning)
            when (settlement.holdReason) {
                "FUNDING_BLOCKED" -> Text(stringResource(R.string.settlement_funding_blocked), fontSize = 12.sp, color = AppColors.Warning)
                "WORK_TERMINATED" -> Text(stringResource(R.string.settlement_work_terminated), fontSize = 12.sp, color = AppColors.Warning)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            com.workernotfound.app.core.designsystem.component.AppSmallButton(stringResource(R.string.history_open_job), onJob)
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, color, background) = when (status) {
        "COMPLETED" -> Triple(R.string.history_completed, AppColors.Success, AppColors.SuccessBg)
        "NO_SHOW" -> Triple(R.string.history_no_show, AppColors.Danger, AppColors.DangerBg)
        "CANCELED" -> Triple(R.string.history_canceled, AppColors.TextSub, AppColors.Border)
        else -> Triple(R.string.history_failed, AppColors.Warning, AppColors.WarningBg)
    }
    AppBadge(text = stringResource(label), background = background, contentColor = color)
}

@Composable
private fun HistoryInfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = AppColors.TextSub, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, color = AppColors.TextMain)
    }
}
