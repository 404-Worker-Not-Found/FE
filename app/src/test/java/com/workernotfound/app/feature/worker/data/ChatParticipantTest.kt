package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.data.remote.ChatRoomResponseDto
import org.junit.Assert.*
import org.junit.Test

class ChatParticipantTest {
    @Test fun `counterpart follows actual participant IDs for either role`() {
        val room = ChatRoomResponseDto(7, 12, 9, ownerMemberId = 2, workerMemberId = 4)
        assertEquals(4L, room.counterpart(2))
        assertEquals(2L, room.counterpart(4))
        assertNull(room.counterpart(8))
        assertNull(room.counterpart(null))
        assertNull(room.copy(workerMemberId = null).counterpart(2))
    }
}
