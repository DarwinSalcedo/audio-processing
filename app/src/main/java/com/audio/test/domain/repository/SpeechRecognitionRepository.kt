package com.audio.test.domain.repository

import com.audio.test.data.repository.WordProcess
import kotlinx.coroutines.flow.Flow

interface SpeechRecognitionRepository {
    /**
     * Starts listening and emits recognized text (partial and final).
     */
    fun startListening(): Flow<WordProcess>

    suspend fun stopListening()
    
    suspend fun initialize()
}
