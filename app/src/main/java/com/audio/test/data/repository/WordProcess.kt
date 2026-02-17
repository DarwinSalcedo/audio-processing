package com.audio.test.data.repository

sealed class WordProcess(val words: String, val isResult: Boolean) {
    class Partial(words: String) : WordProcess(words, false)
    class Final(words: String) : WordProcess(words, true)
}
