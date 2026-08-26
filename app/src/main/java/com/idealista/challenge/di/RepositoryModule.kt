package com.idealista.challenge.di

import com.idealista.challenge.data.repository.AdsRepositoryImpl
import com.idealista.challenge.domain.repository.AdsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAdsRepository(impl: AdsRepositoryImpl): AdsRepository
}
