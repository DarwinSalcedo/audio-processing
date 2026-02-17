package com.learn.easy.domain.model

data class LineResult(
    val originalText: String,
    val spokenText: String,
    val accuracy: Int,
    val words: List<WordResult>
)

data class WordResult(
    val originalWord: String,
    val isMatched: Boolean
)
