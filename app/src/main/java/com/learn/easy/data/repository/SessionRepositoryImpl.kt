package com.learn.easy.data.repository

import com.learn.easy.data.local.SessionDao
import com.learn.easy.data.local.SessionEntity
import com.learn.easy.domain.model.Session
import com.learn.easy.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override fun getAllSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { entity ->
                Session(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    originalText = entity.originalText,
                    spokenText = entity.spokenText,
                    score = entity.score,
                    audioPath = entity.audioPath,
                    lines = entity.lines
                )
            }
        }
    }

    override suspend fun saveSession(session: Session): Long {
        val entity = SessionEntity(
            timestamp = session.timestamp,
            originalText = session.originalText,
            spokenText = session.spokenText,
            score = session.score,
            audioPath = session.audioPath,
            lines = session.lines
        )
        return sessionDao.insertSession(entity)
    }

    override suspend fun getSessionById(sessionId: Long): Session? {
        return sessionDao.getSessionById(sessionId)?.let { entity ->
            Session(
                id = entity.id,
                timestamp = entity.timestamp,
                originalText = entity.originalText,
                spokenText = entity.spokenText,
                score = entity.score,
                audioPath = entity.audioPath,
                lines = entity.lines
            )
        }
    }

    override suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteSession(sessionId)
    }
}
