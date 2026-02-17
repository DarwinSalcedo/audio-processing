package com.learn.easy.domain.usecase

import com.learn.easy.domain.model.Session
import com.learn.easy.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(): Flow<List<Session>> {
        return repository.getAllSessions()
    }
}
