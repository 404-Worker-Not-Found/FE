package com.workernotfound.app.feature.worker.viewmodel

import com.workernotfound.app.feature.worker.domain.model.ChatRoom

/** 채팅 목록 상태 (UI spec: 채팅 탭). */
data class WorkerChatListUiState(
    val isLoading: Boolean = true,
    val rooms: List<ChatRoom> = emptyList(),
    val errorMessage: String? = null,
)
