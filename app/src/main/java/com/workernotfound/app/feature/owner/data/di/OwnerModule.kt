package com.workernotfound.app.feature.owner.data.di

import com.workernotfound.app.feature.owner.data.OwnerApplicantRepositoryImpl
import com.workernotfound.app.feature.owner.data.OwnerHomeRepositoryImpl
import com.workernotfound.app.feature.owner.data.OwnerWorkRepositoryImpl
import com.workernotfound.app.feature.owner.domain.repository.OwnerApplicantRepository
import com.workernotfound.app.feature.owner.domain.repository.OwnerHomeRepository
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
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

    @Binds
    @Singleton
    abstract fun bindOwnerApplicantRepository(impl: OwnerApplicantRepositoryImpl): OwnerApplicantRepository

    @Binds
    @Singleton
    abstract fun bindOwnerWorkRepository(impl: OwnerWorkRepositoryImpl): OwnerWorkRepository
}
