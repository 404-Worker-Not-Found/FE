package com.workernotfound.app.feature.notification.ui

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
import com.workernotfound.app.feature.notification.domain.AppNotification

@Composable
fun NotificationScreen(onBack: () -> Unit, onOpen: (AppNotification) -> Unit,
    modifier: Modifier = Modifier, viewModel: NotificationViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationContent(state, onBack, { viewModel.load() }, { viewModel.load(true) },
        { viewModel.read(it, onOpen) }, modifier)
}
@Composable
private fun NotificationContent(state: NotificationUiState, onBack: () -> Unit, onRefresh: () -> Unit,
    onMore: () -> Unit, onOpen: (AppNotification) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        AppTopBar(stringResource(R.string.notifications_title), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(AppDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            item {
                Text(stringResource(R.string.notifications_unread, state.unread))
                TextButton(onRefresh, enabled = !state.busy) { Text(stringResource(R.string.notifications_refresh)) }
                if (state.busy) CircularProgressIndicator()
                state.error?.let { Text(it, color = AppColors.Danger) }
                if (!state.busy && state.error == null && state.items.isEmpty()) Text(stringResource(R.string.notifications_empty))
            }
            items(state.items, key = { it.id }) { item ->
                AppCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(notificationTitle(item.type)), style = MaterialTheme.typography.titleMedium)
                    Text(item.occurredAt.replace('T', ' '), color = AppColors.TextSub)
                    Text(stringResource(if (item.read) R.string.notifications_read else R.string.notifications_new), color = AppColors.TextSub)
                    TextButton({ onOpen(item) }, enabled = !state.busy) { Text(stringResource(R.string.notifications_open)) }
                }
            }
            if (state.next != null) item {
                TextButton(onMore, enabled = !state.busy) { Text(stringResource(R.string.notifications_more)) }
            }
        }
    }
}
private fun notificationTitle(type: String): Int = when (type) {
    "MatchConfirmed" -> R.string.notifications_match
    "ApplicationRejected" -> R.string.notifications_rejected
    "WorkCheckedIn" -> R.string.notifications_attendance
    "WorkStarted" -> R.string.notifications_started
    "WorkCompleted" -> R.string.notifications_completed
    "WorkNoShow" -> R.string.notifications_no_show
    "WorkCanceled" -> R.string.notifications_canceled
    "FUNDING_REVIEW_REQUIRED" -> R.string.notifications_funding
    else -> R.string.notifications_update
}
