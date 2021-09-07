package com.project.weatherapp.di

import com.project.weatherapp.data.Repository
import com.project.weatherapp.data.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class Repository {

    @Binds
    @Singleton
    abstract fun provideRepository(impl: RepositoryImpl): Repository
}