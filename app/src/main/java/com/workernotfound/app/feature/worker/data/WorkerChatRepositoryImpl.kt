package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.datastore.SessionStore
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.data.remote.ChatMessageRequestDto
import com.workernotfound.app.feature.worker.data.remote.ChatMessageResponseDto
import com.workernotfound.app.feature.worker.data.remote.ChatRoomResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkerChatApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.domain.model.ChatMessage
import com.workernotfound.app.feature.worker.domain.model.ChatRoom
import com.workernotfound.app.feature.worker.domain.repository.WorkerChatRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import com.workernotfound.app.feature.worker.data.remote.nextCursor
import com.workernotfound.app.feature.worker.data.remote.confirm

/**
 * Mock chat repository (UI spec: 채팅). Room ids match matched application ids
 * (r1, r2 …) so "채팅방으로 이동"(3-5)이 해당 대화방으로 연결된다.
 */
class WorkerChatRepositoryImpl @Inject constructor(private val backend: BackendAccess,
    private val api: WorkerChatApiService,
    private val jobs: WorkerJobApiService,
    private val sessions: SessionStore,
    private val realtime: com.workernotfound.app.feature.worker.data.remote.ChatRealtime,
) : WorkerChatRepository {
    override fun changes(roomId: String): kotlinx.coroutines.flow.Flow<Unit> =
        if (BuildConfig.USE_DEMO_DATA) kotlinx.coroutines.flow.emptyFlow() else realtime.changes(roomId.toLong())

    override suspend fun sync(roomId: String, afterId: Long): List<ChatMessage> {
        if (BuildConfig.USE_DEMO_DATA) return emptyList()
        backend.requireSession()
        val result = mutableListOf<ChatMessage>()
        var after = afterId
        do {
            val page = api.sync(roomId.toLong(), after).requireData()
            val next = page.nextCursor(after)
            result += page.content.map { it.toDomain() }
            if (next == null) break
            after = next
        } while (true)
        return result.distinctBy { it.id }.sortedBy { it.id.toLong() }
    }
    override suspend fun read(roomId: String, lastVisibleId: Long): Long {
        if (BuildConfig.USE_DEMO_DATA) return lastVisibleId
        backend.requireSession()
        val response = api.read(roomId.toLong(), com.workernotfound.app.feature.worker.data.remote.ChatReadBody(lastVisibleId)).requireData()
        return response.confirm(roomId, lastVisibleId)
    }

    override suspend fun send(roomId: String, clientMessageId: String, text: String): ChatMessage {
        if (BuildConfig.USE_DEMO_DATA) return ChatMessage(clientMessageId, text, true, "")
        backend.requireSession()
        return api.send(roomId.toLong(), ChatMessageRequestDto(clientMessageId, text)).requireData().toDomain()
    }

    override suspend fun getRooms(): List<ChatRoom> {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val rooms = mutableListOf<ChatRoom>()
            var page = 0
            do {
                val response = api.rooms(page).requireData()
                for (room in response.content) rooms += roomToDomain(room)
                page++
            } while (page < response.totalPages)
            return rooms
        }
        delay(200)
        return ROOMS
    }

    override suspend fun getRoom(roomId: String): ChatRoom {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            return roomToDomain(api.room(roomId.toLong()).requireData())
        }
        delay(100)
        return ROOMS.firstOrNull { it.id == roomId }
            ?: ChatRoom(roomId, "채팅방", "", "", "")
    }

    override suspend fun getMessages(roomId: String): List<ChatMessage> {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val messages = mutableListOf<ChatMessageResponseDto>()
            var beforeId: Long? = null
            do {
                val response = api.messages(roomId.toLong(), beforeId).requireData()
                messages += response.content
                val next = response.nextBeforeId
                if (!response.hasNext || next == null || next == beforeId) break
                beforeId = next
            } while (true)
            return messages.distinctBy { it.messageId }.sortedBy { it.messageId }.map { it.toDomain() }
        }
        delay(200)
        return MESSAGES[roomId] ?: DEFAULT_MESSAGES
    }

    private suspend fun roomToDomain(room: ChatRoomResponseDto): ChatRoom {
        val job = jobs.detail(room.jobPostId).requireData()
        val latest = api.messages(room.chatRoomId, size = 1).requireData().content.firstOrNull()
        return ChatRoom(room.chatRoomId.toString(), job.storeName, job.categoryName, latest?.content.orEmpty(), latest?.createdAt.orEmpty(), room.otherReadPosition?.lastReadMessageId, room.unreadCount)
    }

    private suspend fun ChatMessageResponseDto.toDomain() = ChatMessage(
        messageId.toString(), content, senderMemberId == kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { sessions.read()?.memberId }, createdAt,
    )

    private companion object {
        val ROOMS = listOf(
            ChatRoom("r1", "1번 편의점", "편의점", "네, 내일 9시까지 와주세요!", "오후 2:10"),
            ChatRoom("r2", "패스트 식당", "음식점", "매칭됐습니다. 잘 부탁드려요!", "오후 1:32"),
            ChatRoom("u1", "다정있는 비치", "음식점", "혹시 오늘 저녁 가능하세요?", "오전 11:05"),
        )

        val MESSAGES = mapOf(
            "r1" to listOf(
                ChatMessage("m1", "안녕하세요! 매칭됐네요 :)", fromMe = false, "오후 2:05"),
                ChatMessage("m2", "네 잘 부탁드립니다!", fromMe = true, "오후 2:07"),
                ChatMessage("m3", "내일 09:00까지 편의점으로 와주세요.", fromMe = false, "오후 2:09"),
                ChatMessage("m4", "혹시 유니폼 따로 챙길까요?", fromMe = true, "오후 2:09"),
                ChatMessage("m5", "네, 내일 9시까지 와주세요!", fromMe = false, "오후 2:10"),
            ),
            "r2" to listOf(
                ChatMessage("m1", "재매칭 수락 감사합니다!", fromMe = false, "오후 1:30"),
                ChatMessage("m2", "매칭됐습니다. 잘 부탁드려요!", fromMe = false, "오후 1:32"),
            ),
            "u1" to listOf(
                ChatMessage("m1", "혹시 오늘 저녁 가능하세요?", fromMe = false, "오전 11:05"),
            ),
        )

        val DEFAULT_MESSAGES = listOf(
            ChatMessage("m1", "안녕하세요! 매칭됐습니다 :)", fromMe = false, "방금"),
        )
    }
}
