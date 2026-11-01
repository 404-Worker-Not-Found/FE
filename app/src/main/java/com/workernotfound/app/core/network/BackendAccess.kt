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
}
