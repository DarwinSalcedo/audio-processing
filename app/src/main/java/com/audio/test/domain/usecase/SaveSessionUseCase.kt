package com.audio.test.domain.usecase

import com.audio.test.domain.model.Session
import com.audio.test.domain.repository.SessionRepository
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(session: Session): Long {
        return repository.saveSession(session)
    }
}
