package com.workernotfound.app.core.network

import android.content.Context
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.datastore.SessionStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendAccess @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessions: SessionStore,
) {
    suspend fun requireSession() {
        if (kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { sessions.read() } == null) throw IllegalStateException(context.getString(R.string.backend_login_required))
    }

    /** Unconnected features must never simulate successful server changes in live mode. */
    fun requireDemo() {
        if (!BuildConfig.USE_DEMO_DATA) throw IllegalStateException(context.getString(R.string.backend_unavailable))
    }

    fun message(error: Throwable): String = when (error) {
        is HttpException -> context.getString(when (error.code()) {
            401 -> R.string.backend_login_required
            403 -> R.string.backend_forbidden
            404 -> R.string.backend_not_found
            409 -> R.string.backend_conflict
            else -> R.string.backend_request_failed
        })
        is IOException -> context.getString(R.string.backend_connection_failed)
        is IllegalStateException -> error.message ?: context.getString(R.string.backend_request_failed)
        else -> context.getString(R.string.backend_request_failed)
    }

    /** Debug-only report. The response body may contain personal data; never include request data. */
    fun diagnostic(error: Throwable): String? {
        if (!BuildConfig.DEBUG) return null
        val response = (error as? HttpException)?.response()
        val request = response?.raw()?.request
        val responseBody = runCatching { response?.errorBody()?.string() }.getOrNull()
        val serverCode = if (error is ApiException) error.code else runCatching {
            responseBody?.let { com.google.gson.JsonParser.parseString(it).asJsonObject.get("code")?.asString }
        }.getOrNull()
        val safeCode = serverCode?.takeIf { it.matches(Regex("[A-Z][A-Z0-9_]{0,79}")) }
        return listOf(
            "Android 오류 보고",
            "시간: ${java.time.Instant.now()}",
            "앱: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            "서버: ${request?.url?.host ?: java.net.URI(BuildConfig.API_BASE_URL).host}",
            "요청: ${request?.method ?: "확인 불가"} ${request?.url?.encodedPath ?: "확인 불가"}",
            "HTTP: ${response?.code() ?: "응답 없음"}",
            "서버 코드: ${safeCode ?: "확인 불가"}",
            "오류 종류: ${error.javaClass.simpleName}",
            context.getString(R.string.auth_response_body_label),
            responseBody ?: context.getString(R.string.auth_response_body_missing),
            context.getString(R.string.auth_diagnostic_sensitive),
        ).joinToString("\n")
    }
}
