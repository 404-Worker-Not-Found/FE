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
) : AuthRepository {
    override suspend fun login(email: String, password: String): AuthSession = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val response = api.login(LoginRequestDto(email, password, store.deviceId())).requireData()
        store.save(StoredSession(response.memberId, response.role, response.tokenResponse.accessToken, response.tokenResponse.refreshToken))
        AuthSession(response.memberId, response.role)
    }

    override suspend fun logout() = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val session = store.read() ?: return@withContext
        // Keep the session available for retry if the server cannot revoke it.
        if (!api.logout(LogoutRequestDto(session.refreshToken)).success) {
            throw ApiException("LOGOUT_FAILED")
        }
        store.clearIfCurrent(session)
    }

    override suspend fun currentSession(): AuthSession? = kotlinx.coroutines.withContext(Dispatchers.IO) { store.read()?.toDomain() }
}
