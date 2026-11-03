package com.workernotfound.app.core.network

import com.workernotfound.app.core.datastore.SessionStorage
import com.workernotfound.app.feature.auth.data.remote.AuthApiService
import com.workernotfound.app.feature.auth.data.remote.TokenReissueRequestDto
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException

class SessionInterceptor(private val store: SessionStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val session = store.read()
        val token = session?.accessToken
        val request = chain.request().newBuilder().apply {
            if (token != null) {
                header("Authorization", "Bearer $token")
                tag(RequestMember::class.java, RequestMember(session.memberId))
            }
        }.build()
        return chain.proceed(request)
    }
}

/** One retry, single refresh across concurrent 401s, refresh never uses this authenticated client. */
class SessionAuthenticator(private val store: SessionStorage, private val api: AuthApiService) : Authenticator {
    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? = synchronized(refreshLock) {
        if (response.priorResponse != null) return null
        val session = store.read() ?: return null
        if (response.request.tag(RequestMember::class.java)?.id != session.memberId) return null
        if (response.request.header("Authorization") != "Bearer ${session.accessToken}") {
            return response.request.newBuilder().header("Authorization", "Bearer ${session.accessToken}").build()
        }
        val refreshed = try {
            api.reissue(TokenReissueRequestDto(session.refreshToken, store.deviceId())).execute()
        } catch (_: IOException) {
            return null // transient connection failure must not destroy a valid refresh session
        }
        if (!refreshed.isSuccessful) refreshed.errorBody()?.close()
        if (refreshed.code() == 401) {
            store.clearIfCurrent(session)
            return null
        }
        val tokens = refreshed.body()?.takeIf { it.success }?.data ?: return null
        if (tokens.accessToken.isNullOrBlank() || tokens.refreshToken.isNullOrBlank()) return null
        if (!store.replace(session, session.copy(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken))) return null
        response.request.newBuilder().header("Authorization", "Bearer ${tokens.accessToken}").build()
    }
}

internal data class RequestMember(val id: Long)
