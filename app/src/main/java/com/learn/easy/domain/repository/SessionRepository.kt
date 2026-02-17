package com.learn.easy.domain.repository

import com.learn.easy.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun saveSession(session: Session): Long
    suspend fun getSessionById(sessionId: Long): Session?
    suspend fun deleteSession(sessionId: Long)
}
