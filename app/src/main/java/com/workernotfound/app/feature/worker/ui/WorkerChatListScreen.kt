package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.clickable
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
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkerChatListContent(uiState = uiState, onRoomClick = onRoomClick, modifier = modifier)
}

@Composable
private fun WorkerChatListContent(
    uiState: WorkerChatListUiState,
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
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
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = room.storeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = room.lastMessage,
                    fontSize = 13.sp,
                    color = AppColors.TextSub,
                    maxLines = 1,
                )
            }
            Text(text = room.lastTimeText, fontSize = 11.sp, color = AppColors.TextPlaceholder)
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
