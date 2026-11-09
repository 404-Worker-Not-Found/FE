package com.workernotfound.app.feature.owner.data.di

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.feature.owner.data.OwnerSettlementRepositoryImpl
import com.workernotfound.app.feature.owner.data.remote.OwnerSettlementApiService
import com.workernotfound.app.feature.owner.domain.repository.OwnerSettlementRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OwnerSettlementModule {
    @Provides @Singleton
    fun api(client: OkHttpClient): OwnerSettlementApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build().create(OwnerSettlementApiService::class.java)

    @Provides @Singleton
    fun repository(impl: OwnerSettlementRepositoryImpl): OwnerSettlementRepository = impl
}
