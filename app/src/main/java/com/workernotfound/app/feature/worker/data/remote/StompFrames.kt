package com.workernotfound.app.feature.worker.data.remote

import java.io.IOException

/** Only protocol metadata is exposed; frames must never be logged (CONNECT contains a token). */
internal class StompFrame(val command: String, val headers: Map<String, String>)
internal class StompFrames {
    private var buffer = byteArrayOf()
    fun feed(bytes: ByteArray): List<StompFrame> {
        if (buffer.size + bytes.size > 65_536) throw IOException("Chat frame limit exceeded")
        buffer += bytes
        val result = mutableListOf<StompFrame>()
        while (buffer.isNotEmpty()) {
            var offset = 0
            while (offset < buffer.size && buffer[offset] in listOf(10.toByte(), 13.toByte())) offset++
            if (offset > 0) buffer = buffer.copyOfRange(offset, buffer.size)
            if (buffer.isEmpty()) break
            val commandEnd = newline(0)
            if (commandEnd < 0) break
            val command = text(0, commandEnd).removeSuffix("\r")
            val headers = linkedMapOf<String, String>()
            var position = commandEnd + 1
            var bodyStart = -1
            while (true) {
                val end = newline(position)
                if (end < 0) break
                val line = text(position, end).removeSuffix("\r")
                position = end + 1
                if (line.isEmpty()) { bodyStart = position; break }
                val colon = line.indexOf(':')
                if (colon <= 0) throw IOException("Invalid chat header")
                val escaped = command !in setOf("CONNECT", "CONNECTED")
                val key = if (escaped) unescape(line.substring(0, colon)) else line.substring(0, colon)
                val value = if (escaped) unescape(line.substring(colon + 1)) else line.substring(colon + 1)
                headers.putIfAbsent(key, value)
            }
            if (bodyStart < 0) break
            val length = headers["content-length"]?.let {
                it.toIntOrNull()?.takeIf { count -> count in 0..65_536 } ?: throw IOException("Invalid chat length")
            }
            val bodyEnd = if (length != null) bodyStart + length else {
                var end = bodyStart
                while (end < buffer.size && buffer[end] != 0.toByte()) end++
                end
            }
            if (bodyEnd >= buffer.size) break
            if (buffer[bodyEnd] != 0.toByte()) throw IOException("Invalid chat terminator")
            result += StompFrame(command, headers)
            buffer = buffer.copyOfRange(bodyEnd + 1, buffer.size)
        }
        return result
    }
    private fun newline(start: Int): Int {
        for (i in start until buffer.size) if (buffer[i] == 10.toByte()) return i
        return -1
    }
    private fun text(start: Int, end: Int) = buffer.copyOfRange(start, end).toString(Charsets.UTF_8)
    private fun unescape(value: String): String = buildString {
        var i = 0
        while (i < value.length) {
            val c = value[i++]
            if (c != '\\') append(c)
            else {
                if (i >= value.length) throw IOException("Invalid chat escape")
                append(when (value[i++]) { 'r' -> '\r'; 'n' -> '\n'; 'c' -> ':'; '\\' -> '\\'; else -> throw IOException("Invalid chat escape") })
            }
        }
    }
}
