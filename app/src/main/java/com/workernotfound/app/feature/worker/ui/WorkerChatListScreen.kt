package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.feature.worker.domain.model.ChatRoom
import com.workernotfound.app.feature.worker.viewmodel.WorkerChatListUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerChatListViewModel

@Composable
fun WorkerChatListScreen(
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerChatListViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    androidx.lifecycle.compose.LifecycleResumeEffect(viewModel) { viewModel.load(); onPauseOrDispose { } }
    Column(if (onBack != null) modifier.statusBarsPadding() else modifier) {
        if (onBack != null) com.workernotfound.app.core.designsystem.component.AppTopBar(
            androidx.compose.ui.res.stringResource(com.workernotfound.app.R.string.chat_title), onBack = onBack)
        uiState.errorMessage?.let {
            Text(it, color = AppColors.Danger)
            com.workernotfound.app.core.designsystem.component.AppSmallButton(androidx.compose.ui.res.stringResource(com.workernotfound.app.R.string.chat_retry), viewModel::load)
        }
        WorkerChatListContent(uiState = uiState, onRoomClick = onRoomClick, showTitle = onBack == null, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun WorkerChatListContent(
    uiState: WorkerChatListUiState,
    onRoomClick: (String) -> Unit,
    showTitle: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (showTitle) Text(
            text = "채팅",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
            modifier = Modifier.padding(
                start = AppDimens.screenPadding,
                end = AppDimens.screenPadding,
                top = 20.dp,
                bottom = 8.dp,
            ),
        )
        when {
            uiState.isLoading -> LoadingBox()
            uiState.rooms.isEmpty() -> EmptyBox()
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppDimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
            ) {
                items(items = uiState.rooms, key = { it.id }) { room ->
                    ChatRoomRow(room = room, onClick = { onRoomClick(room.id) })
                }
            }
        }
    }
}

@Composable
private fun ChatRoomRow(room: ChatRoom, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(vertical = AppDimens.cardGap), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
        Box(Modifier.size(ChatLayout.avatarSize).background(AppTheme.role.accentTint,
            androidx.compose.foundation.shape.RoundedCornerShape(ChatLayout.avatarRadius)), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Outlined.Person,
                contentDescription = null, tint = AppColors.TextSub, modifier = Modifier.size(ChatLayout.avatarIcon))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ChatLayout.smallGap)) {
            Text(text = room.counterpartName ?: room.counterpartId?.let { androidx.compose.ui.res.stringResource(com.workernotfound.app.R.string.chat_member_number, it) }
                ?: androidx.compose.ui.res.stringResource(com.workernotfound.app.R.string.chat_counterpart),
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, color = AppColors.TextMain)
            Text(room.lastMessage.ifBlank { androidx.compose.ui.res.stringResource(com.workernotfound.app.R.string.chat_no_messages) },
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = AppColors.TextSub,
                maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        Column(Modifier.widthIn(max = ChatLayout.storeWidth), horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(ChatLayout.smallGap)) {
            Text(room.storeName, style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = AppColors.TextSub, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            if (room.unreadCount > 0) androidx.compose.material3.Badge(containerColor = AppColors.Danger,
                contentColor = androidx.compose.ui.graphics.Color.White) {
                Text(if (room.unreadCount > 999) androidx.compose.ui.res.stringResource(com.workernotfound.app.R.string.chat_unread_overflow)
                    else room.unreadCount.toString())
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.role.accent)
    }
}

@Composable
private fun EmptyBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "아직 대화가 없어요.", color = AppColors.TextSub)
    }
}
