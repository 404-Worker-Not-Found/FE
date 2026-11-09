package com.workernotfound.app.feature.mypage.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProfileCameraFiles {
    suspend fun create(context: Context): String = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Cannot create camera cache")
        File.createTempFile("capture_", ".jpg", directory).name
    }

    fun uri(context: Context, fileName: String): Uri = FileProvider.getUriForFile(
        context, "${context.packageName}.profile.photos", File(context.cacheDir, "$DIRECTORY/$fileName"),
    )

    suspend fun discard(context: Context, fileName: String) = withContext(Dispatchers.IO) {
        discard(File(context.cacheDir, DIRECTORY), fileName)
    }

    /** Never accept arbitrary paths or touch gallery originals. */
    internal fun discard(directory: File, fileName: String): Boolean {
        if (!fileName.startsWith("capture_") || !fileName.endsWith(".jpg")) return false
        return try {
            val file = File(directory, fileName)
            file.canonicalFile.parentFile == directory.canonicalFile && (!file.exists() || file.delete())
        } catch (error: IOException) {
            false
        } catch (error: SecurityException) {
            false
        }
    }

    private const val DIRECTORY = "profile_camera"
}
