package com.workernotfound.app.feature.auth.domain

/** Passwords remain in memory only; the data layer persists encrypted tokens. */
interface AuthRepository {
    suspend fun login(email: String, password: String): AuthSession
    suspend fun logout()
    suspend fun currentSession(): AuthSession?
}

data class AuthSession(val memberId: Long, val role: String)
