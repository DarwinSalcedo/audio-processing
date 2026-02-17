package com.learn.easy.domain.model

data class ProcessResult(
    val updatedLines: List<Line>,
    val additionalSpokenText: String?,
    val shouldAdvanceLine: Boolean,
    val hasStarted: Boolean,
    val completedLineIndex: Int? = null
)
