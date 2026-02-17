package com.audio.test.di

import com.audio.test.data.repository.SessionRepositoryImpl
import com.audio.test.data.repository.VoskSpeechRecognitionRepositoryImpl
import com.audio.test.domain.repository.SessionRepository
import com.audio.test.domain.repository.SpeechRecognitionRepository
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
