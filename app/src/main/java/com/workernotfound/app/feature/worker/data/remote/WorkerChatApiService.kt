package com.workernotfound.app.feature.worker.data.remote

import com.workernotfound.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkerChatApiService {
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
data class ChatRoomResponseDto(val chatRoomId: Long, val jobPostId: Long)
data class ChatMessagePageResponseDto(val content: List<ChatMessageResponseDto>, val nextBeforeId: Long?, val hasNext: Boolean)
data class ChatMessageResponseDto(val messageId: Long, val senderMemberId: Long, val content: String, val createdAt: String)
data class ChatMessageRequestDto(val clientMessageId: String, val content: String)
