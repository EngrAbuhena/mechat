package com.mechat.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt DI root. Modules for data/, crypto/, network/ and domain/
 * will be added here in later steps.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
