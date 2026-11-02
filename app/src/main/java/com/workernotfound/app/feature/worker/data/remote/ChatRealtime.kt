package com.workernotfound.app.feature.worker.data.remote

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.datastore.SessionStore
import com.workernotfound.app.core.datastore.SessionStorage
import javax.inject.Inject
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okio.ByteString

/** A socket carries invalidation events only. REST sync remains the authoritative message source. */
class ChatRealtime internal constructor(private val client: OkHttpClient, private val sessions: SessionStorage, private val socketUrl: String) {
    @Inject constructor(client: OkHttpClient, sessions: SessionStore) : this(client, sessions, BuildConfig.API_BASE_URL + "api/chat-rooms/ws")
    fun changes(roomId: Long) = callbackFlow {
        require(roomId > 0)
        val original = withContext(Dispatchers.IO) { sessions.read() }
        if (original == null) { close(IOException("Chat login required")); return@callbackFlow }
        val connected = AtomicBoolean(false)
        val lastReceived = AtomicLong(System.nanoTime())
        val lastSent = AtomicLong(System.nanoTime())
        val receiveInterval = AtomicLong(0)
        val sendInterval = AtomicLong(0)
        val parser = StompFrames()
        val destination = "/topic/chat-rooms/$roomId"
        val listener = object : WebSocketListener() {
            override fun onOpen(socket: WebSocket, response: Response) {
                val current = try { sessions.read() } catch (_: Exception) { null }
                if (current == null || current.memberId != original.memberId) {
                    close(IOException("Chat session changed")); socket.cancel(); return
                }
                val host = response.request.url.host
                socket.send("CONNECT\naccept-version:1.2\nhost:$host\nheart-beat:10000,10000\nAuthorization:Bearer ${current.accessToken}\n\n\u0000")
                lastSent.set(System.nanoTime())
            }
            override fun onMessage(socket: WebSocket, text: String) = consume(socket, text.toByteArray(Charsets.UTF_8))
            override fun onMessage(socket: WebSocket, bytes: ByteString) = consume(socket, bytes.toByteArray())
            private fun consume(socket: WebSocket, bytes: ByteArray) {
                lastReceived.set(System.nanoTime())
                try {
                    for (frame in parser.feed(bytes)) when (frame.command) {
                        "CONNECTED" -> {
                            if (!connected.compareAndSet(false, true)) throw IOException("Duplicate chat connection")
                            val beat = frame.headers["heart-beat"].orEmpty().split(',').map { it.toLongOrNull() ?: 0 }
                            receiveInterval.set(if ((beat.getOrNull(0) ?: 0) > 0) maxOf(10_000, beat[0]) else 0)
                            sendInterval.set(if ((beat.getOrNull(1) ?: 0) > 0) maxOf(10_000, beat[1]) else 0)
                            socket.send("SUBSCRIBE\nid:room\ndestination:$destination\nack:auto\n\n\u0000")
                            lastSent.set(System.nanoTime())
                            trySend(Unit)
                        }
                        "MESSAGE" -> if (connected.get() && frame.headers["destination"] == destination) trySend(Unit)
                        "ERROR" -> { close(IOException("Chat connection rejected")); socket.cancel() }
                    }
                } catch (_: IOException) { close(IOException("Invalid chat connection")); socket.cancel() }
            }
            override fun onClosing(socket: WebSocket, code: Int, reason: String) { socket.close(code, null) }
            override fun onClosed(socket: WebSocket, code: Int, reason: String) { close(IOException("Chat disconnected")) }
            override fun onFailure(socket: WebSocket, t: Throwable, response: Response?) { close(IOException("Chat connection failed")) }
        }
        // HTTPS is upgraded to a secure WebSocket by OkHttp. No browser Origin is added.
        val socket = client.newWebSocket(Request.Builder().url(socketUrl).build(), listener)
        val heartbeat = launch {
            while (isActive) {
                delay(1000)
                val now = System.nanoTime()
                val timeout = if (connected.get()) receiveInterval.get().takeIf { it > 0 }?.let { it * 3 } else 15_000L
                if (timeout != null && (now - lastReceived.get()) / 1_000_000 > timeout) {
                    close(IOException("Chat heartbeat timed out")); socket.cancel(); break
                }
                val interval = sendInterval.get()
                if (connected.get() && interval > 0 && (now - lastSent.get()) / 1_000_000 >= interval) {
                    socket.send("\n"); lastSent.set(now)
                }
            }
        }
        awaitClose { heartbeat.cancel(); socket.cancel() }
    }
}
