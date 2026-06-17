package com.workernotfound.app.feature.worker.data.di

import com.workernotfound.app.feature.worker.data.WorkerHomeRepositoryImpl
import com.workernotfound.app.feature.worker.data.WorkerJobRepositoryImpl
import com.workernotfound.app.feature.worker.domain.repository.WorkerHomeRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Feature-scoped Hilt module (decision: prefer feature-scoped DI modules). */
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    @Binds
    @Singleton
    abstract fun bindWorkerHomeRepository(impl: WorkerHomeRepositoryImpl): WorkerHomeRepository

    @Binds
    @Singleton
    abstract fun bindWorkerJobRepository(impl: WorkerJobRepositoryImpl): WorkerJobRepository
}
