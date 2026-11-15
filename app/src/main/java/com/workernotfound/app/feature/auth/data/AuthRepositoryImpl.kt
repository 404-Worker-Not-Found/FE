package com.workernotfound.app.feature.auth.data

import com.workernotfound.app.core.datastore.SessionStore
import com.workernotfound.app.core.datastore.StoredSession
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.auth.data.remote.AuthApiService
import com.workernotfound.app.feature.auth.data.remote.LoginRequestDto
import com.workernotfound.app.feature.auth.data.remote.LogoutRequestDto
import com.workernotfound.app.feature.auth.domain.AuthRepository
import com.workernotfound.app.feature.auth.domain.AuthSession
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val store: SessionStore,
    private val profiles: com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService,
    private val push: com.workernotfound.app.feature.notification.data.PushRegistration,
) : AuthRepository {
    override suspend fun login(email: String, password: String): AuthSession = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val response = api.login(LoginRequestDto(email, password, store.deviceId())).requireData()
        response.validateSession()
        store.save(StoredSession(response.memberId, response.role, response.tokenResponse.accessToken, response.tokenResponse.refreshToken))
        registerPush()
        AuthSession(response.memberId, response.role)
    }

    override suspend fun logout() = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val session = store.read() ?: return@withContext
        runCatching { push.unregister() }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }
        // Keep the session available for retry if the server cannot revoke it.
        if (!api.logout(LogoutRequestDto(session.refreshToken)).success) {
            throw ApiException("LOGOUT_FAILED")
        }
        store.clearIfCurrent(session)
    }

    override suspend fun currentSession(): AuthSession? = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val session = store.read() ?: return@withContext null
        // Validates on the server and uses the existing 401 refresh mechanism.
        val profile = profiles.get().requireData()
        if (profile.memberId != session.memberId || profile.role != session.role) throw ApiException("INVALID_SESSION")
        registerPush()
        session.toDomain()
    }

    private suspend fun registerPush() {
        runCatching { push.registerCurrentToken() }.onFailure { error ->
            if (error is kotlinx.coroutines.CancellationException) throw error
            android.util.Log.w("PushRegistration", "FCM registration failed: ${error.javaClass.simpleName}")
        }
    }
}
