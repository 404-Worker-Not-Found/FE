package com.workernotfound.app.feature.worker.data.di

import com.workernotfound.app.feature.worker.data.WorkerProfileRepositoryImpl
import com.workernotfound.app.feature.worker.domain.repository.WorkerProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerProfileModule {
    @Binds
    abstract fun bindWorkerProfileRepository(impl: WorkerProfileRepositoryImpl): WorkerProfileRepository
}
