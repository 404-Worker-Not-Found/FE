package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.ChatMessage
import com.workernotfound.app.feature.worker.domain.model.ChatRoom

/** Chat data source (UI spec: 채팅). Mock-first; real-time(WebSocket)은 후속. */
interface WorkerChatRepository {
    suspend fun getRooms(): List<ChatRoom>
    suspend fun getRoom(roomId: String): ChatRoom
    suspend fun getMessages(roomId: String): List<ChatMessage>
}
