package com.workernotfound.app.feature.notification.ui

import com.workernotfound.app.core.ui.KoreanDateTime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/** 알림: 새로고침은 상단 아이콘·당겨서 새로고침, 카드 전체를 눌러 열고, 안 읽은 알림은 점과 배경으로 구분. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationContent(state: NotificationUiState, onBack: () -> Unit, onRefresh: () -> Unit,
    onMore: () -> Unit, onOpen: (AppNotification) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(AppTheme.role.screenBackground).statusBarsPadding()) {
        AppTopBar(
            stringResource(R.string.notifications_title),
            onBack = onBack,
            actions = {
                IconButton(onClick = onRefresh, enabled = !state.busy) {
                    Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.notifications_refresh), tint = AppColors.TextSub)
                }
            },
        )
        val firstLoad = state.busy && state.items.isEmpty()
        when {
            firstLoad -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.role.accent)
            }
            else -> PullToRefreshBox(
                isRefreshing = state.busy && state.items.isNotEmpty(),
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppDimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
                ) {
                    item {
                        if (state.unread > 0) {
                            Text(
                                stringResource(R.string.notifications_unread, state.unread),
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.role.accentPressed,
                            )
                        }
                        state.error?.let { Text(it, color = AppColors.Danger, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)) }
                    }
                    if (state.items.isEmpty() && state.error == null) item {
                        Box(Modifier.fillParentMaxHeight(0.7f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.notifications_empty), color = AppColors.TextSub)
                        }
                    }
                    items(state.items, key = { it.id }) { item ->
                        NotificationCard(item, enabled = !state.busy, onOpen = { onOpen(item) })
                    }
                    if (state.next != null) item {
                        AppSecondaryButton(stringResource(R.string.notifications_more), onMore, enabled = !state.busy)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: AppNotification, enabled: Boolean, onOpen: () -> Unit) {
    val unread = !item.read
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (unread) AppTheme.role.accentTint else AppColors.CardSurface, RoundedCornerShape(AppRadius.card))
            .clickable(enabled = enabled, onClick = onOpen)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Unread marker; read items keep the space so titles stay aligned.
        Box(
            Modifier.size(8.dp).background(if (unread) AppTheme.role.accent else androidx.compose.ui.graphics.Color.Transparent, CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(notificationTitle(item.type)),
                fontSize = 15.sp,
                fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium,
                color = if (unread) AppColors.TextMain else AppColors.TextSub,
            )
            Spacer(Modifier.height(4.dp))
            Text(KoreanDateTime.relative(item.occurredAt), fontSize = 12.sp, color = AppColors.TextPlaceholder)
        }
        Text("›", fontSize = 18.sp, color = AppColors.TextPlaceholder)
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
