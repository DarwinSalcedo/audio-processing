package com.learn.easy.di

import com.learn.easy.data.repository.SessionRepositoryImpl
import com.learn.easy.data.repository.VoskSpeechRecognitionRepositoryImpl
import com.learn.easy.domain.repository.SessionRepository
import com.learn.easy.domain.repository.SpeechRecognitionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindSpeechRecognitionRepository(
        impl: VoskSpeechRecognitionRepositoryImpl
    ): SpeechRecognitionRepository
}
