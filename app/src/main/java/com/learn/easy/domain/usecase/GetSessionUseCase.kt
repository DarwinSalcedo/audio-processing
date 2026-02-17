package com.learn.easy.domain.usecase

import com.learn.easy.domain.model.Session
import com.learn.easy.domain.repository.SessionRepository
import javax.inject.Inject

class GetSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long): Session? {
        return repository.getSessionById(sessionId)
    }
}
