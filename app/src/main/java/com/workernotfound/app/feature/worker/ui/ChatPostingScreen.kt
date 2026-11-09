package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.feature.location.AddressMap
import com.workernotfound.app.feature.worker.viewmodel.WorkerJobDetailViewModel

/** The exact job referenced by the conversation; read-only for either participant role. */
@Composable
fun ChatPostingScreen(onBack: () -> Unit, modifier: Modifier = Modifier,
    viewModel: WorkerJobDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().background(AppColors.CardSurface).statusBarsPadding()) {
        AppTopBar(stringResource(R.string.chat_posting_title), onBack = onBack)
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.padding(AppDimens.screenPadding))
            state.errorMessage != null -> Column(Modifier.padding(AppDimens.screenPadding)) {
                Text(requireNotNull(state.errorMessage), color = AppColors.Danger)
                TextButton(viewModel::load) { Text(stringResource(R.string.chat_retry)) }
            }
            else -> state.detail?.let { job ->
                Column(Modifier.verticalScroll(rememberScrollState()).padding(AppDimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
                    Text(job.storeName, style = MaterialTheme.typography.headlineSmall)
                    Text(job.category, color = AppColors.TextSub)
                    Text(job.address)
                    AddressMap(job.address, Modifier.fillMaxWidth().height(AppDimens.locationMapHeight))
                    Text(job.workDate)
                    Text(job.workTimeRange)
                    Text(job.totalWorkHours)
                    Text(stringResource(R.string.chat_posting_wage, job.finalWage), style = MaterialTheme.typography.titleLarge)
                    Text(job.description)
                }
            }
        }
    }
}
