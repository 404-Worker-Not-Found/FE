package com.workernotfound.app.feature.cancellation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.feature.cancellation.domain.*

@Composable
fun CancellationScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: CancellationViewModel = hiltViewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = s.context
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        AppTopBar(stringResource(R.string.cancellation_title), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(AppDimens.screenPadding), verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            item {
                Text(stringResource(R.string.cancellation_notice), color = AppColors.TextSub)
                TextButton(viewModel::load, enabled = !s.busy) { Text(stringResource(R.string.cancellation_refresh)) }
                if (s.busy) CircularProgressIndicator()
                s.error?.let { Text(it, color = AppColors.Danger) }
                context?.let { Text(it.store, style = MaterialTheme.typography.titleMedium); Text(it.description) }
            }
            if (context != null) {
                if (context.canRequest(s.now)) item {
                    OutlinedTextField(s.reason, viewModel::reason, label = { Text(stringResource(R.string.cancellation_reason)) },
                        enabled = !s.busy && s.key == null, isError = s.reason.length > 500, modifier = Modifier.fillMaxWidth())
                    AppPrimaryButton(stringResource(R.string.cancellation_request), { viewModel.confirm(CancellationIntent(CancellationAction.REQUEST)) },
                        enabled = !s.busy && s.reason.isNotBlank() && s.reason.length <= 500)
                } else item { Text(stringResource(R.string.cancellation_unavailable), color = AppColors.TextSub) }
                items(context.requests, key = { it.id }) { request ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(cancellationStatus(request.status)))
                        Text(request.reason)
                        Text(request.createdAt.replace('T', ' '), color = AppColors.TextSub)
                        if (context.withinWindow(s.now)) {
                            if (request.canWithdraw(context.member)) TextButton({ viewModel.confirm(CancellationIntent(CancellationAction.WITHDRAW, request.id)) }, enabled = !s.busy) { Text(stringResource(R.string.cancellation_withdraw)) }
                            if (request.canRespond(context.member)) Row {
                                TextButton({ viewModel.confirm(CancellationIntent(CancellationAction.APPROVE, request.id)) }, enabled = !s.busy) { Text(stringResource(R.string.cancellation_approve)) }
                                TextButton({ viewModel.confirm(CancellationIntent(CancellationAction.REJECT, request.id)) }, enabled = !s.busy) { Text(stringResource(R.string.cancellation_reject)) }
                            }
                        }
                    }
                }
            }
        }
    }
    if (s.confirm != null) AlertDialog(onDismissRequest = { viewModel.confirm(null) }, title = { Text(stringResource(R.string.cancellation_title)) },
        text = { Text(stringResource(if (s.confirm?.action == CancellationAction.APPROVE) R.string.cancellation_confirm_approve else R.string.cancellation_confirm)) },
        confirmButton = { TextButton(viewModel::perform, enabled = !s.busy) { Text(stringResource(R.string.cancellation_confirm_button)) } },
        dismissButton = { TextButton({ viewModel.confirm(null) }, enabled = !s.busy) { Text(stringResource(R.string.cancellation_close)) } })
}
private fun cancellationStatus(status: String) = when (status) {
    "PENDING" -> R.string.cancellation_pending
    "APPROVED" -> R.string.cancellation_approved
    "REJECTED" -> R.string.cancellation_rejected
    "WITHDRAWN" -> R.string.cancellation_withdrawn
    "EXPIRED" -> R.string.cancellation_expired
    else -> R.string.cancellation_unknown
}
