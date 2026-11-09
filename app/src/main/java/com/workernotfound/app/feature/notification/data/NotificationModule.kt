package com.workernotfound.app.feature.notification.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.notification.domain.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationRepositoryImpl @Inject constructor(private val api: NotificationApi, private val backend: BackendAccess) : NotificationRepository {
    override suspend fun page(before: Long?): NotificationPage {
        backend.requireSession()
        return api.page(before).requireData().toDomain(before)
    }
    override suspend fun unreadCount(): Long {
        backend.requireSession()
        return api.unread().requireData().unreadCount
    }
    override suspend fun markRead(id: Long): AppNotification {
        backend.requireSession()
        val result = api.read(id).requireData()
        if (result.notificationId != id || result.readAt == null) throw ApiException("NOTIFICATION_NOT_READ")
        return result.toDomain()
    }
}
@Module @InstallIn(SingletonComponent::class)
object NotificationModule {
    @Provides fun api(client: OkHttpClient): NotificationApi = Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
        .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(NotificationApi::class.java)
    @Provides fun repository(impl: NotificationRepositoryImpl): NotificationRepository = impl
}
