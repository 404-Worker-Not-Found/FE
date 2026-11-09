package com.workernotfound.app.feature.worker.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkerChatApiService {
    @GET("api/chat-rooms/me/{id}/messages/sync")
    suspend fun sync(@Path("id") id: Long, @Query("afterId") afterId: Long, @Query("size") size: Int = 100): ApiResponse<ChatSyncDto>
    @PUT("api/chat-rooms/me/{id}/read-position")
    suspend fun read(@Path("id") id: Long, @Body body: ChatReadBody): ApiResponse<ChatReadDto>
    @GET("api/chat-rooms/me")
    suspend fun rooms(@Query("page") page: Int, @Query("size") size: Int = 100): ApiResponse<ChatRoomPageResponseDto>
    @GET("api/chat-rooms/me/{id}")
    suspend fun room(@Path("id") id: Long): ApiResponse<ChatRoomResponseDto>
    @GET("api/chat-rooms/me/{id}/messages")
    suspend fun messages(@Path("id") id: Long, @Query("beforeId") beforeId: Long? = null, @Query("size") size: Int = 100): ApiResponse<ChatMessagePageResponseDto>
    @POST("api/chat-rooms/me/{id}/messages")
    suspend fun send(@Path("id") id: Long, @Body request: ChatMessageRequestDto): ApiResponse<ChatMessageResponseDto>
}
data class ChatRoomPageResponseDto(val content: List<ChatRoomResponseDto>, val totalPages: Int)
data class ChatRoomResponseDto(val chatRoomId: Long, val jobPostId: Long, val matchingId: Long,
    val otherReadPosition: ChatParticipantReadDto? = null, val unreadCount: Long = 0,
    val ownerMemberId: Long? = null, val workerMemberId: Long? = null)
data class ChatParticipantReadDto(val lastReadMessageId: Long?)
data class ChatSyncDto(val content: List<ChatMessageResponseDto>, val nextAfterId: Long?, val hasNext: Boolean)
data class ChatReadBody(val lastReadMessageId: Long)
data class ChatReadDto(val chatRoomId: Long, val lastReadMessageId: Long)
internal fun ChatSyncDto.nextCursor(after: Long): Long? {
    if (content.any { it.messageId <= after }) throw com.workernotfound.app.core.network.ApiException("INVALID_CHAT_MESSAGE_ORDER")
    if (!hasNext) return null
    val next = nextAfterId ?: throw com.workernotfound.app.core.network.ApiException("MISSING_CHAT_CURSOR")
    if (next <= after || content.isEmpty() || next != content.maxOf { it.messageId })
        throw com.workernotfound.app.core.network.ApiException("INVALID_CHAT_CURSOR")
    return next
}
internal fun ChatReadDto.confirm(roomId: String, requested: Long): Long {
    if (chatRoomId.toString() != roomId || lastReadMessageId < requested)
        throw com.workernotfound.app.core.network.ApiException("CHAT_READ_NOT_CONFIRMED")
    return lastReadMessageId
}
data class ChatMessagePageResponseDto(val content: List<ChatMessageResponseDto>, val nextBeforeId: Long?, val hasNext: Boolean)
data class ChatMessageResponseDto(val messageId: Long, val senderMemberId: Long, val content: String, val createdAt: String)
data class ChatMessageRequestDto(val clientMessageId: String, val content: String)
