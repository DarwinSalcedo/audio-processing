package com.learn.easy.data

data class Phrase(
    val en: String,
    val es: String
)

data class TopicCategory(
    val topic: String,
    val list: List<Phrase>
)