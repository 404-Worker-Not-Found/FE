package com.workernotfound.app.feature.cancellation.domain

data class Cancellation(val id: Long, val workId: Long, val requester: Long, val reason: String,
    val status: String, val createdAt: String) {
    fun canWithdraw(member: Long) = status == "PENDING" && requester == member
    fun canRespond(member: Long) = status == "PENDING" && requester != member
}
data class CancellationContext(val member: Long, val store: String, val description: String,
    val scheduled: Boolean, val checkedIn: Boolean, val confirmed: Boolean, val startsAtMillis: Long,
    val requests: List<Cancellation>) {
    fun withinWindow(now: Long) = confirmed && scheduled && !checkedIn && now < startsAtMillis
    fun canRequest(now: Long) = withinWindow(now) && requests.none { it.status == "PENDING" }
}
enum class CancellationAction { REQUEST, APPROVE, REJECT, WITHDRAW }
interface CancellationRepository {
    suspend fun context(workId: Long): CancellationContext
    suspend fun request(workId: Long, key: String, reason: String): Cancellation
    suspend fun respond(workId: Long, requestId: Long, action: CancellationAction): Cancellation
}
