package com.workernotfound.app.feature.worker.domain.model

/** A chat conversation with a store owner (UI spec: 하단 내비 '채팅', 3-5 채팅방 이동). */
data class ChatRoom(
    val id: String,
    val storeName: String,
    val category: String,
    val lastMessage: String,
    val lastTimeText: String,
)

/** One message inside a chat room. [fromMe] = 알바생 본인이 보낸 메시지. */
data class ChatMessage(
    val id: String,
    val text: String,
    val fromMe: Boolean,
    val timeText: String,
)
