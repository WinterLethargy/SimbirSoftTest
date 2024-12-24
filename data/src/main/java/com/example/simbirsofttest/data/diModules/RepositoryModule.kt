package com.example.simbirsofttest.data.diModules

import com.example.simbirsofttest.data.repositories.DealRepository
import com.example.simbirsofttest.data.repositories.IDealRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    internal abstract fun provideDealRepository(
        dealRepository: DealRepository
    ): IDealRepository
}