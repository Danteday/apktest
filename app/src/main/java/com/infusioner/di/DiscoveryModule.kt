package com.infusioner.di

import com.infusioner.domain.discovery.DiscoveryRepository
import com.infusioner.data.discovery.DiscoveryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiscoveryModule {
    @Provides
    @Singleton
    fun provideDiscoveryRepository(impl: DiscoveryRepositoryImpl): DiscoveryRepository = impl
}
