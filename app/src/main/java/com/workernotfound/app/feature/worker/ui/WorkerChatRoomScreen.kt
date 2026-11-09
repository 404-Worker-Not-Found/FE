package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.ui.KoreanDateTime
import com.workernotfound.app.feature.worker.domain.model.ChatMessage
import com.workernotfound.app.feature.worker.viewmodel.WorkerChatRoomUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerChatRoomViewModel

@Composable
fun WorkerChatRoomScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerChatRoomViewModel = hiltViewModel(),
    onJobClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.startUpdates()
        onPauseOrDispose { viewModel.stopUpdates() }
    }
    WorkerChatRoomContent(
        uiState = uiState,
        onBack = onBack,
        onJobClick = onJobClick,
        onDraftChange = viewModel::onDraftChange,
        onSend = viewModel::send,
        onRetry = viewModel::load,
        onVisible = viewModel::visible,
        modifier = modifier,
    )
}

@Composable
private fun WorkerChatRoomContent(
    uiState: WorkerChatRoomUiState,
    onBack: () -> Unit,
    onJobClick: (String) -> Unit,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: () -> Unit,
    onVisible: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.role.screenBackground)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = uiState.room?.let { room -> room.counterpartName ?: room.counterpartId?.let {
            stringResource(R.string.chat_member_number, it) } } ?: stringResource(R.string.chat_counterpart), onBack = onBack)
        uiState.room?.let { room ->
            TextButton(onClick = { room.jobId?.let(onJobClick) }, enabled = room.jobId != null,
                modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.chat_open_posting, room.storeName), maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
        if (uiState.reconnecting) Text(stringResource(R.string.chat_reconnecting), color = AppColors.TextSub)
        uiState.errorMessage?.let {
            Text(it, color = AppColors.Danger)
            TextButton(onRetry) { Text(stringResource(R.string.chat_retry)) }
        }
        when {
            uiState.isLoading -> LoadingBox()
            uiState.room == null -> Spacer(Modifier.weight(1f))
            else -> {
                MessageList(messages = uiState.messages, otherReadId = uiState.room?.otherLastReadId,
                    onVisible = onVisible, modifier = Modifier.weight(1f))
                ChatInputBar(draft = uiState.draft, onDraftChange = onDraftChange, onSend = onSend)
            }
        }
    }
}

@Composable
private fun MessageList(messages: List<ChatMessage>, otherReadId: Long?, onVisible: (Long) -> Unit, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var previousCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(messages.lastOrNull()?.id) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        if (messages.isNotEmpty() && (previousCount == 0 || lastVisible >= previousCount - 1)) listState.scrollToItem(messages.lastIndex)
        previousCount = messages.size
    }
    LaunchedEffect(listState, lifecycle) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.mapNotNull { it.key.toString().toLongOrNull() }.maxOrNull() }
            .distinctUntilChanged().flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .collect { id -> if (id != null) onVisible(id) }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(items = messages, key = { _, message -> message.id }) { index, message ->
            val date = KoreanDateTime.date(message.timeText)
            val previousDate = messages.getOrNull(index - 1)?.let { KoreanDateTime.date(it.timeText) }
            Column {
                if (date != null && date != previousDate) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = KoreanDateTime.day(message.timeText),
                            color = AppColors.TextSub,
                            fontSize = 12.sp,
                            modifier = Modifier.background(AppColors.CardSurface, RoundedCornerShape(AppRadius.pill))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
                MessageBubble(message)
                if (message.fromMe && otherReadId != null && (message.id.toLongOrNull() ?: Long.MAX_VALUE) <= otherReadId)
                    Text(stringResource(R.string.chat_read), color = AppColors.TextSub)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (message.fromMe) {
            MessageTimestamp(message.timeText)
            Spacer(Modifier.width(6.dp))
            MessageTextBubble(message, Modifier.weight(1f, fill = false))
        } else {
            MessageTextBubble(message, Modifier.weight(1f, fill = false))
            Spacer(Modifier.width(6.dp))
            MessageTimestamp(message.timeText)
        }
    }
}

@Composable
private fun MessageTextBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .widthIn(max = 280.dp)
            .background(
                if (message.fromMe) AppTheme.role.accent else AppColors.CardSurface,
                RoundedCornerShape(AppRadius.card),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = message.text,
            color = if (message.fromMe) Color.White else AppColors.TextMain,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun MessageTimestamp(raw: String) {
    Text(text = KoreanDateTime.time(raw), fontSize = 12.sp, color = AppColors.TextSub)
}

@Composable
private fun ChatInputBar(draft: String, onDraftChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardSurface)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .background(AppColors.WorkerSurface, RoundedCornerShape(AppRadius.pill))
                .border(AppDimens.borderWidth, AppColors.Border, RoundedCornerShape(AppRadius.pill))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (draft.isEmpty()) {
                Text(text = "메시지 입력", color = AppColors.TextPlaceholder, fontSize = 14.sp)
            }
            BasicTextField(
                value = draft,
                onValueChange = onDraftChange,
                singleLine = true,
                textStyle = TextStyle(color = AppColors.TextMain, fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(AppTheme.role.accent, RoundedCornerShape(AppRadius.pill))
                .clickable(onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "전송", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.role.accent)
    }
}
