package com.workernotfound.app.feature.notification.data

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.workernotfound.app.core.datastore.SessionStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class PushRegistration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessions: SessionStore,
    private val api: PushTokenApi,
) {
    private val preferences by lazy { context.getSharedPreferences("push_registration", Context.MODE_PRIVATE) }

    suspend fun registerCurrentToken() {
        val token = suspendCancellableCoroutine<String> { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener
                if (task.isSuccessful) continuation.resume(task.result)
                else continuation.resumeWithException(task.exception ?: IllegalStateException("FCM token unavailable"))
            }
        }
        register(token)
    }

    suspend fun register(token: String) {
        val session = sessions.read() ?: return
        val id = api.register(PushTokenRequestDto(token)).requireData().deviceTokenId
        if (id > 0 && sessions.read()?.memberId == session.memberId) {
            preferences.edit().putLong("device_token_id", id).putLong("member_id", session.memberId).apply()
            android.util.Log.i("PushRegistration", "FCM token registered with server")
        }
    }

    suspend fun unregister() {
        val session = sessions.read() ?: return
        val id = preferences.getLong("device_token_id", 0)
        if (id > 0 && preferences.getLong("member_id", 0) == session.memberId) {
            api.remove(id)
            preferences.edit().clear().apply()
        }
    }
}
