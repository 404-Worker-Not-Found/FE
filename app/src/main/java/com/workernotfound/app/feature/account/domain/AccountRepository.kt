package com.workernotfound.app.feature.account.domain

enum class ContactChannel { EMAIL, PHONE }
data class AccountCommand(val id: String, val status: String) {
    val terminal get() = status in setOf("SUCCEEDED", "REJECTED")
}
interface AccountRepository {
    suspend fun send(channel: ContactChannel, target: String)
    suspend fun change(key: String, channel: ContactChannel, target: String, code: String): AccountCommand
    suspend fun contactStatus(key: String): AccountCommand
    suspend fun withdraw(key: String): AccountCommand
    suspend fun withdrawalStatus(key: String): AccountCommand
    suspend fun clearCompletedSession()
}
