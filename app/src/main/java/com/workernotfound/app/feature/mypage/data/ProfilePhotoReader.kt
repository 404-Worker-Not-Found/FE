package com.workernotfound.app.feature.mypage.data

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Validate actual readable content without assuming a provider reports its size. */
object ProfilePhotoReader {
    suspend fun isSupported(resolver: ContentResolver, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        if (resolver.getType(uri) !in setOf("image/jpeg", "image/png")) return@withContext false
        resolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(8192)
            var bytes = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) return@withContext bytes > 0
                bytes += count
                if (bytes > MAX_BYTES) return@withContext false
            }
        }
        false
    }
    private const val MAX_BYTES = 5 * 1024 * 1024
}
