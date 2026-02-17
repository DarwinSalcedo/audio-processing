package com.learn.easy.domain.repository

import com.learn.easy.data.repository.WordProcess
import kotlinx.coroutines.flow.Flow

interface SpeechRecognitionRepository {
    /**
     * Starts listening and emits recognized text (partial and final).
     */
    fun startListening(): Flow<WordProcess>

    suspend fun stopListening()
    
    suspend fun initialize()
}
