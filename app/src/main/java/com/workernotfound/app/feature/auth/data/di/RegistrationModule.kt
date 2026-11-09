package com.workernotfound.app.feature.auth.data.di

import com.workernotfound.app.feature.auth.data.RegistrationRepositoryImpl
import com.workernotfound.app.feature.auth.domain.RegistrationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RegistrationModule {
    @Binds abstract fun registration(impl: RegistrationRepositoryImpl): RegistrationRepository
}
