package com.workernotfound.app.core.network

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.datastore.SessionStore
import com.workernotfound.app.feature.auth.data.AuthRepositoryImpl
import com.workernotfound.app.feature.auth.data.remote.AuthApiService
import com.workernotfound.app.feature.auth.domain.AuthRepository
import com.workernotfound.app.feature.owner.data.remote.OwnerJobApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerApplicationApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerChatApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerWorkApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun memberProfiles(client: OkHttpClient): com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService =
        retrofit(client).create(com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService::class.java)
    private fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun plainClient() = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        // Never forward a Bearer token to a redirect target.
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    @Provides @Singleton fun authApi(): AuthApiService = retrofit(plainClient()).create(AuthApiService::class.java)

    @Provides @Singleton fun client(store: SessionStore, api: AuthApiService): OkHttpClient = plainClient().newBuilder()
        .addInterceptor(SessionInterceptor(store))
        .authenticator(SessionAuthenticator(store, api))
        .build()

    @Provides @Singleton fun jobs(client: OkHttpClient): WorkerJobApiService = retrofit(client).create(WorkerJobApiService::class.java)
    @Provides @Singleton fun works(client: OkHttpClient): WorkerWorkApiService = retrofit(client).create(WorkerWorkApiService::class.java)
    @Provides @Singleton fun ownerJobs(client: OkHttpClient): OwnerJobApiService = retrofit(client).create(OwnerJobApiService::class.java)
    @Provides @Singleton fun chat(client: OkHttpClient): WorkerChatApiService = retrofit(client).create(WorkerChatApiService::class.java)
    @Provides @Singleton fun applications(client: OkHttpClient): WorkerApplicationApiService = retrofit(client).create(WorkerApplicationApiService::class.java)
    @Provides @Singleton fun authRepository(impl: AuthRepositoryImpl): AuthRepository = impl
}
