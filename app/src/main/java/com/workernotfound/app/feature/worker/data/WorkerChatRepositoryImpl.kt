package com.workernotfound.app.feature.worker.data

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
import com.workernotfound.app.feature.worker.data.remote.nextCursor
import com.workernotfound.app.feature.worker.data.remote.confirm

/** Chat repository (UI spec: 채팅): REST history/sync/read plus WSS/STOMP change notifications. */
class WorkerChatRepositoryImpl @Inject constructor(private val backend: BackendAccess,
    private val api: WorkerChatApiService,
    private val jobs: WorkerJobApiService,
    private val sessions: SessionStore,
    private val applicants: com.workernotfound.app.feature.owner.data.remote.OwnerApplicantApiService,
    private val realtime: com.workernotfound.app.feature.worker.data.remote.ChatRealtime,
) : WorkerChatRepository {
    private val names = mutableMapOf<Pair<Long, Long>, String>()
    override fun changes(roomId: String): kotlinx.coroutines.flow.Flow<Unit> =
        realtime.changes(roomId.toLong())

    override suspend fun sync(roomId: String, afterId: Long): List<ChatMessage> {
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
        backend.requireSession()
        val response = api.read(roomId.toLong(), com.workernotfound.app.feature.worker.data.remote.ChatReadBody(lastVisibleId)).requireData()
        return response.confirm(roomId, lastVisibleId)
    }

    override suspend fun send(roomId: String, clientMessageId: String, text: String): ChatMessage {
        backend.requireSession()
        return api.send(roomId.toLong(), ChatMessageRequestDto(clientMessageId, text)).requireData().toDomain()
    }

    override suspend fun getRooms(): List<ChatRoom> {
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

    override suspend fun getRoom(roomId: String): ChatRoom {
        backend.requireSession()
        return roomToDomain(api.room(roomId.toLong()).requireData())
    }

    override suspend fun getMessages(roomId: String): List<ChatMessage> {
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

    private suspend fun roomToDomain(room: ChatRoomResponseDto): ChatRoom {
        val job = jobs.detail(room.jobPostId).requireData()
        val latest = api.messages(room.chatRoomId, size = 1).requireData().content.firstOrNull()
        val self = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { sessions.read()?.memberId }
        val counterpart = room.counterpart(self)
        val name = if (self == room.ownerMemberId && counterpart != null) workerName(room.jobPostId, counterpart) else null
        return ChatRoom(room.chatRoomId.toString(), job.storeName, job.categoryName, latest?.content.orEmpty(), latest?.createdAt.orEmpty(),
            room.otherReadPosition?.lastReadMessageId, room.unreadCount, room.jobPostId.toString(), counterpart, name)
    }

    private suspend fun workerName(jobId: Long, workerId: Long): String? {
        val key = jobId to workerId
        names[key]?.let { return it }
        return try {
            var page = 0
            var batch: Long? = null
            do {
                val result = applicants.list(jobId, page, batch).requireData()
                if (page == 0) batch = result.scoreBatchId
                result.applicants.firstOrNull { it.workerMemberId == workerId }?.workerName
                    ?.takeIf { it.isNotBlank() }?.let { names[key] = it; return it }
                page++
            } while (page < result.totalPages)
            null
        } catch (_: retrofit2.HttpException) { null }
        catch (_: java.io.IOException) { null }
        catch (_: com.workernotfound.app.core.network.ApiException) { null }
    }

    private suspend fun ChatMessageResponseDto.toDomain() = ChatMessage(
        messageId.toString(), content, senderMemberId == kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { sessions.read()?.memberId }, createdAt,
    )
}

internal fun ChatRoomResponseDto.counterpart(self: Long?): Long? = when (self) {
    null -> null
    ownerMemberId -> workerMemberId?.takeIf { it > 0 && it != self }
    workerMemberId -> ownerMemberId?.takeIf { it > 0 && it != self }
    else -> null
}
