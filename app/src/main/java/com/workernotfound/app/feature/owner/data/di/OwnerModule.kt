package com.workernotfound.app.feature.owner.data.di

import com.workernotfound.app.feature.owner.data.OwnerHomeRepositoryImpl
import com.workernotfound.app.feature.owner.domain.repository.OwnerHomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Feature-scoped Hilt module (decision: prefer feature-scoped DI modules). */
@Module
@InstallIn(SingletonComponent::class)
abstract class OwnerModule {

    @Binds
    @Singleton
    abstract fun bindOwnerHomeRepository(impl: OwnerHomeRepositoryImpl): OwnerHomeRepository
}
