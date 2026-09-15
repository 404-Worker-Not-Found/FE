package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.ChatMessage
import com.workernotfound.app.feature.worker.domain.model.ChatRoom
import com.workernotfound.app.feature.worker.domain.repository.WorkerChatRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock chat repository (UI spec: 채팅). Room ids match matched application ids
 * (r1, r2 …) so "채팅방으로 이동"(3-5)이 해당 대화방으로 연결된다.
 */
class WorkerChatRepositoryImpl @Inject constructor() : WorkerChatRepository {

    override suspend fun getRooms(): List<ChatRoom> {
        delay(200)
        return ROOMS
    }

    override suspend fun getRoom(roomId: String): ChatRoom {
        delay(100)
        return ROOMS.firstOrNull { it.id == roomId }
            ?: ChatRoom(roomId, "채팅방", "", "", "")
    }

    override suspend fun getMessages(roomId: String): List<ChatMessage> {
        delay(200)
        return MESSAGES[roomId] ?: DEFAULT_MESSAGES
    }

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
