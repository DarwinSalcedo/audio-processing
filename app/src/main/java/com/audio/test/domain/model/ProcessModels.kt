package com.audio.test.domain.model

data class Line(
    val id: Int,
    val text: String,
    val words: List<Word>,
    val isCompleted: Boolean = false,
    val accuracy: Int = 0 
)

data class Word(
    val text: String,
    val isMatched: Boolean = false
)
