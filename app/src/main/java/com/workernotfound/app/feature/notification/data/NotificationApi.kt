package com.workernotfound.app.feature.notification.data

import com.workernotfound.app.core.network.ApiResponse
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.notification.domain.AppNotification
import com.workernotfound.app.feature.notification.domain.NotificationPage
import retrofit2.http.*

interface NotificationApi {
    @GET("api/notifications/me") suspend fun page(@Query("beforeId") before: Long?, @Query("size") size: Int = 20): ApiResponse<NotificationPageDto>
    @GET("api/notifications/me/unread-count") suspend fun unread(): ApiResponse<UnreadDto>
    @PATCH("api/notifications/me/{id}/read") suspend fun read(@Path("id") id: Long): ApiResponse<NotificationDto>
}
data class NotificationDto(val notificationId: Long, val type: String, val jobPostId: Long,
    val applicationId: Long?, val workId: Long?, val occurredAt: String, val readAt: String?) {
    fun toDomain() = AppNotification(notificationId, type, jobPostId, applicationId, workId, occurredAt, readAt != null)
}
data class UnreadDto(val unreadCount: Long)
data class NotificationPageDto(val content: List<NotificationDto>, val nextBeforeId: Long?, val hasNext: Boolean) {
    fun toDomain(before: Long?): NotificationPage {
        if (hasNext && (content.isEmpty() || nextBeforeId == null || nextBeforeId <= 0 ||
                (before != null && nextBeforeId >= before))) throw ApiException("INVALID_NOTIFICATION_CURSOR")
        return NotificationPage(content.map { it.toDomain() }, if (hasNext) nextBeforeId else null)
    }
}
