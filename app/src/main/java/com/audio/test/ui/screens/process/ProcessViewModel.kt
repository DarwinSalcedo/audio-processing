package com.audio.test.ui.screens.process

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audio.test.data.repository.WordProcess
import com.audio.test.domain.model.Line
import com.audio.test.domain.model.LineResult
import com.audio.test.domain.model.Session
import com.audio.test.domain.model.Word
import com.audio.test.domain.model.WordResult
import com.audio.test.domain.repository.SpeechRecognitionRepository
import com.audio.test.domain.usecase.SaveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcessViewModel @Inject constructor(
    private val speechRepository: SpeechRecognitionRepository,
    private val saveSessionUseCase: SaveSessionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val originalText: String = checkNotNull(savedStateHandle["text"])

    private val _navigateToResult = Channel<Long>()
    val navigateToResult = _navigateToResult.receiveAsFlow()

    // Accumulate all spoken text for final result
    private val _spokenTextBuilder = StringBuilder()

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines = _lines.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(0)
    val currentLineIndex = _currentLineIndex.asStateFlow()

    private val _hasStarted = MutableStateFlow(false)
    val hasStarted = _hasStarted.asStateFlow()

    init {
        initializeLines()
    }

    private fun initializeLines() {
        // Split by newlines or punctuation to get sentences/lines
        val rawLines = originalText.trim().split(Regex("(?<=[.!?])\\s+"))

        val lineList = rawLines.mapIndexed { index, text ->
            val words = text.trim().split("\\s+".toRegex()).map {
                Word(it, false)
            }
            Line(index, text.trim(), words)
        }
        _lines.value = lineList
    }

    fun toggleRecording() {
        if (_isListening.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        _isListening.value = true
        viewModelScope.launch {
            speechRepository.startListening().collectLatest { partialOrFinal ->
                processSpeech(partialOrFinal)
            }
        }
    }

    private fun processSpeech(spoken: WordProcess) {
        val currentIndex = _currentLineIndex.value
        val currentLines = _lines.value
        if (currentIndex >= currentLines.size) return
        if (spoken.words.trim().isEmpty()) return

        _hasStarted.value = true

        val currentLine = currentLines[currentIndex]
        val spokenWords = spoken.words.lowercase().split("\\s+".toRegex())

        if (spoken is WordProcess.Partial) {
            // PARTIAL: Only update visual matching, do not advance line yet
            val updatedWords = currentLine.words.map { originalWord ->
                if (originalWord.isMatched) {
                    originalWord
                } else {
                    val cleanOriginal = originalWord.text.lowercase().replace(Regex("[^a-z0-9]"), "")
                    if (cleanOriginal.isEmpty()) {
                        originalWord
                    } else {
                        // Strict match only: Word must exactly equal the spoken word
                        val isMatch = spokenWords.any { spokenWord ->
                             spokenWord == cleanOriginal
                        }
                        originalWord.copy(isMatched = isMatch)
                    }
                }
            }

            val updatedLine = currentLine.copy(words = updatedWords)
            val newLines = currentLines.toMutableList()
            newLines[currentIndex] = updatedLine
            _lines.value = newLines
        } else if (spoken is WordProcess.Final) {
            // FINAL: Re-evaluate with final text, commit score, and ADVANCE
            val updatedWords = currentLine.words.map { originalWord ->
                if (originalWord.isMatched) {
                    originalWord
                } else {
                    val cleanOriginal = originalWord.text.lowercase().replace(Regex("[^a-z0-9]"), "")
                    if (cleanOriginal.isEmpty()) {
                        originalWord
                    } else {
                        val isMatch = spokenWords.any { spokenWord ->
                            spokenWord == cleanOriginal
                        }
                        originalWord.copy(isMatched = isMatch)
                    }
                }
            }

            val matchedCount = updatedWords.count { it.isMatched }
            val totalWords = updatedWords.size
            val accuracy = if (totalWords > 0) (matchedCount * 100) / totalWords else 0

            val updatedLine = currentLine.copy(
                words = updatedWords,
                accuracy = accuracy,
                isCompleted = true
            )

            val newLines = currentLines.toMutableList()
            newLines[currentIndex] = updatedLine
            _lines.value = newLines

            _spokenTextBuilder.append(spoken.words).append(" ")

            if (currentIndex < currentLines.size - 1) {
                _currentLineIndex.value = currentIndex + 1
            } else {
                stopRecording()
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            speechRepository.stopListening()
            _isListening.value = false
            // Do not save immediately. Wait for user action.
        }
    }

    fun finishSession() {
        saveSession()
    }

    fun resetSession() {
        viewModelScope.launch {
            speechRepository.stopListening()
            _isListening.value = false
            _spokenTextBuilder.clear()
            _currentLineIndex.value = 0
            _hasStarted.value = false
            initializeLines()
        }
    }

    private fun saveSession() {
        viewModelScope.launch {
            val currentLines = _lines.value
            val totalAccuracy = currentLines.map { it.accuracy }.average().toInt()

            val lineResults = currentLines.map { line ->
                LineResult(
                    originalText = line.text,
                    spokenText = "", // Not tracking granular spoken text per line yet
                    accuracy = line.accuracy,
                    words = line.words.map { word ->
                        WordResult(
                            originalWord = word.text,
                            isMatched = word.isMatched
                        )
                    }
                )
            }

            val session = Session(
                timestamp = System.currentTimeMillis(),
                originalText = originalText,
                spokenText = _spokenTextBuilder.toString().ifEmpty { "Incomplete session" },
                score = totalAccuracy,
                audioPath = null,
                lines = lineResults
            )
            val id = saveSessionUseCase(session)
            _navigateToResult.send(id)
        }
    }
}
