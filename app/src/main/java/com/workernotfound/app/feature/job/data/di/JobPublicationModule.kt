package com.workernotfound.app.feature.job.data.di

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.feature.job.data.JobPublicationRepositoryImpl
import com.workernotfound.app.feature.job.data.remote.JobPublicationApiService
import com.workernotfound.app.feature.job.domain.repository.JobPublicationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object JobPublicationModule {
    @Provides fun api(client: OkHttpClient): JobPublicationApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create())
        .build().create(JobPublicationApiService::class.java)
    @Provides fun repository(impl: JobPublicationRepositoryImpl): JobPublicationRepository = impl
}
