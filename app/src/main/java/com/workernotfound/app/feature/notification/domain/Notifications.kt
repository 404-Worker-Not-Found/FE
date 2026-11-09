package com.workernotfound.app.feature.notification.domain

data class AppNotification(
    val id: Long, val type: String, val jobId: Long, val applicationId: Long?,
    val workId: Long?, val occurredAt: String, val read: Boolean,
)
data class NotificationPage(val items: List<AppNotification>, val next: Long?)
interface NotificationRepository {
    suspend fun page(before: Long? = null): NotificationPage
    suspend fun unreadCount(): Long
    suspend fun markRead(id: Long): AppNotification
}
