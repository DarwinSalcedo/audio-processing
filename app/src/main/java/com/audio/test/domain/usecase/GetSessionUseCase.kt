package com.audio.test.domain.usecase

import com.audio.test.domain.model.Session
import com.audio.test.domain.repository.SessionRepository
import javax.inject.Inject

class GetSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long): Session? {
        return repository.getSessionById(sessionId)
    }
}
