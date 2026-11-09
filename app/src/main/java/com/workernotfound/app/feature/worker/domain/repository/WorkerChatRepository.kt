package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.ChatMessage
import com.workernotfound.app.feature.worker.domain.model.ChatRoom

/** Chat data source (UI spec: 채팅). Realtime updates arrive over WSS/STOMP. */
interface WorkerChatRepository {
    fun changes(roomId: String): kotlinx.coroutines.flow.Flow<Unit>
    suspend fun sync(roomId: String, afterId: Long): List<ChatMessage>
    suspend fun read(roomId: String, lastVisibleId: Long): Long
    suspend fun send(roomId: String, clientMessageId: String, text: String): ChatMessage

    suspend fun getRooms(): List<ChatRoom>
    suspend fun getRoom(roomId: String): ChatRoom
    suspend fun getMessages(roomId: String): List<ChatMessage>
}
