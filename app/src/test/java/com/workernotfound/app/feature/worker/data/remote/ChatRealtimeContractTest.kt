package com.workernotfound.app.feature.worker.data.remote

import com.workernotfound.app.core.datastore.SessionStorage
import com.workernotfound.app.core.datastore.StoredSession
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okhttp3.*
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ChatRealtimeContractTest {
    @Test fun stompParserHandlesSplitUtf8LengthNullBodyAndCoalescedFrames() {
        val parser = StompFrames()
        assertTrue(parser.feed("\n\r\nCONNE".toByteArray()).isEmpty())
        val body = "가\u0000나".toByteArray()
        val bytes = "CTED\nversion:1.2\n\n\u0000MESSAGE\r\ndestination:/topic/chat-rooms/4\r\ncontent-length:${body.size}\r\n\r\n".toByteArray()+body+byteArrayOf(0)
        val frames = parser.feed(bytes)
        assertEquals(listOf("CONNECTED","MESSAGE"),frames.map { it.command })
        assertEquals("/topic/chat-rooms/4",frames.last().headers["destination"])
    }
    @Test fun stompParserUsesFirstHeaderAndRejectsInvalidEscapeAndOversize() {
        val parser = StompFrames()
        val frame = parser.feed("MESSAGE\nx:a\\cb\nx:second\n\n\u0000".toByteArray()).single()
        assertEquals("a:b",frame.headers["x"])
        assertTrue(runCatching { parser.feed("MESSAGE\nx:bad\\t\n\n\u0000".toByteArray()) }.isFailure)
        assertTrue(runCatching { StompFrames().feed(ByteArray(65_537)) }.isFailure)
    }
    @Test fun websocketConnectAuthenticatesAndSubscribesWithoutBrowserOrigin() = runBlocking {
        val server = MockWebServer()
        val frames = LinkedBlockingQueue<String>()
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onMessage(socket: WebSocket, text: String) {
                frames.offer(text)
                if (text.startsWith("CONNECT\n")) socket.send("CONNECTED\nversion:1.2\nheart-beat:0,0\n\n\u0000")
                if (text.startsWith("SUBSCRIBE\n")) socket.send("MESSAGE\ndestination:/topic/chat-rooms/4\n\n{}\u0000")
            }
        }))
        server.start()
        val client = OkHttpClient()
        try {
            val result = withTimeout(8000) { ChatRealtime(client, MemorySession(), server.url("/api/chat-rooms/ws").toString()).changes(4).take(2).toList() }
            assertEquals(2,result.size)
            val connect = frames.poll(2,TimeUnit.SECONDS)
            assertTrue(connect.contains("Authorization:Bearer mock-access\n"))
            assertTrue(frames.poll(2,TimeUnit.SECONDS).contains("destination:/topic/chat-rooms/4\n"))
            val request = server.takeRequest()
            assertEquals("/api/chat-rooms/ws",request.path)
            assertNull(request.getHeader("Origin"))
        } finally { client.connectionPool.evictAll(); client.dispatcher.executorService.shutdown(); server.shutdown() }
    }
    @Test fun restSyncAndReadPositionFollowPublicContract() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            val api=Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(GsonConverterFactory.create()).build().create(WorkerChatApiService::class.java)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"content":[{"messageId":8,"senderMemberId":2,"content":"hello","createdAt":"2026-11-02T10:00:00"}],"nextAfterId":8,"hasNext":true}}"""))
            assertEquals(8L,api.sync(4,7).requireData().nextCursor(7))
            assertEquals("/api/chat-rooms/me/4/messages/sync?afterId=7&size=100",server.takeRequest().path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"chatRoomId":4,"lastReadMessageId":8}}"""))
            assertEquals(8L,api.read(4,ChatReadBody(8)).requireData().confirm("4",8))
            val request=server.takeRequest();assertEquals("PUT",request.method)
            assertEquals("/api/chat-rooms/me/4/read-position",request.path)
            assertEquals("""{"lastReadMessageId":8}""",request.body.readUtf8())
        } finally { server.shutdown() }
    }
    @Test fun invalidSyncOrWrongReadReceiptCannotAdvanceProgress() {
        assertTrue(runCatching { ChatSyncDto(emptyList(),8,true).nextCursor(7) }.isFailure)
        val message=ChatMessageResponseDto(7,2,"hi","2026-11-02T10:00:00")
        assertTrue(runCatching { ChatSyncDto(listOf(message),7,false).nextCursor(7) }.isFailure)
        assertTrue(runCatching { ChatReadDto(5,8).confirm("4",8) }.isFailure)
        assertTrue(runCatching { ChatReadDto(4,7).confirm("4",8) }.isFailure)
    }
    private class MemorySession : SessionStorage {
        private var current: StoredSession?=StoredSession(1,"WORKER","mock-access","mock-refresh")
        override fun deviceId()="test"
        override fun read()=current
        override fun save(session: StoredSession){current=session}
        override fun clear(){current=null}
        override fun replace(expected: StoredSession,updated: StoredSession):Boolean {if(current!=expected)return false;current=updated;return true}
        override fun clearIfCurrent(expected: StoredSession){if(current==expected)clear()}
    }
}
