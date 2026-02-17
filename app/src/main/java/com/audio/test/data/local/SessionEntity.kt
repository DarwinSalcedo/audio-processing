package com.audio.test.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.audio.test.domain.model.LineResult

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val originalText: String,
    val spokenText: String,
    val score: Int, // Percentage 0-100
    val audioPath: String?, // Path to saved audio file, if any
    val lines: List<LineResult> = emptyList()
)
