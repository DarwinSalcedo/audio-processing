package com.learn.easy.domain.model

data class Session(
    val id: Long = 0,
    val timestamp: Long,
    val originalText: String,
    val spokenText: String,
    val score: Int,
    val audioPath: String?,
    val lines: List<LineResult> = emptyList()
)
