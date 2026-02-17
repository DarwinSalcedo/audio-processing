package com.audio.test.domain.usecase

import com.audio.test.domain.model.Session
import com.audio.test.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(): Flow<List<Session>> {
        return repository.getAllSessions()
    }
}
