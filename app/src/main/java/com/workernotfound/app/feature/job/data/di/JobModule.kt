package com.workernotfound.app.feature.job.data.di

import com.workernotfound.app.feature.job.data.JobPostingRepositoryImpl
import com.workernotfound.app.feature.job.domain.repository.JobPostingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Feature-scoped Hilt module (decision: prefer feature-scoped DI modules). */
@Module
@InstallIn(SingletonComponent::class)
abstract class JobModule {

    @Binds
    @Singleton
    abstract fun bindJobPostingRepository(impl: JobPostingRepositoryImpl): JobPostingRepository
}
